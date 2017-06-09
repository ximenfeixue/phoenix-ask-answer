package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.cache.Cache;
import com.ginkgocap.ywxt.interlocution.model.*;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import com.ginkgocap.ywxt.interlocution.service.PraiseService;
import com.ginkgocap.ywxt.interlocution.utils.AskAnswerJsonUtils;
import com.ginkgocap.ywxt.interlocution.web.Task.DataSyncTask;
import com.ginkgocap.ywxt.user.model.User;
import com.ginkgocap.ywxt.user.service.UserService;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import com.gintong.ywxt.im.model.MessageNotify;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Wang fei on 2017/5/31.
 */
@RestController
@RequestMapping("/praise")
public class PraiseController extends BaseController{

    private final Logger logger = LoggerFactory.getLogger(PraiseController.class);

    @Resource
    private PraiseService praiseService;

    @Resource
    private DataSyncTask dataSyncTask;

    @Resource
    private UserService userService;

    @Resource
    private AnswerService answerService;

    @Resource
    private AskService askService;

    @Resource
    private Cache cache;

    /**
     * 创建点赞
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public InterfaceResult create(HttpServletRequest request) {

        InterfaceResult result = null;
        Praise praise = null;
        Answer answer = null;
        Question question = null;
        List<PartAnswer> partAnswerList = null;

        User user = this.getUser(request);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        try {
            String requestJson = this.getBodyParam(request);
            praise = (Praise)JsonUtils.jsonToBean(requestJson, Praise.class);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        if (praise.getAnswerId() < 0) {
            logger.error("answerId is error");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        try {
            Praise dbPraise = praiseService.getPraiseByUIdAnswerId(praise.getAnswerId(), user.getId());
            if (dbPraise != null) {
                result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
                result.getNotification().setNotifInfo("同一用户只能点赞一次");
                return result;
            }
        } catch (Exception e) {
            logger.error("invoke praise service failed! method : [ getPraiseByUIdAnswerId ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        // 补全 praise
        praise.setAdmirerId(user.getId());
        final short virtual = user.isVirtual() ? (short) 1 : (short) 0;
        praise.setVirtual(virtual);
        try {
            result = praiseService.create(praise);

        } catch (Exception e) {
            logger.error("invoke praiseService failed : method :[ create ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        // 点赞 成功后 所做操作
        if (result.getResponseData() != null && (Long)result.getResponseData() > 0) {

            User dbUser = null;
            try {
                dbUser = userService.selectByPrimaryKey(user.getId());
            } catch (Exception e) {
                logger.error("invoke userService failed !please check userService");
            }
            long answerId = praise.getAnswerId();

            if (answerId < 1) {
                logger.error("param : answerId < 1");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            }
            try {
                answer = answerService.getAnswerById(answerId);

            } catch (Exception e) {
                logger.error("invoke answerService failed! method :[getAnswerById]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
            // 放到 redis 中 key：answerId value: userId
            addPraiseUId2Redis(answerId, user.getId());

            // 更新答案表
            try {
                updateAnswer(dbUser, answer);

            } catch (Exception e) {
                logger.error("invoke answer service failed! method :[ getAnswerById, updateAnswer ]");
            }
            // 更新问题表
            try {
                question = askService.getQuestionById(answer.getQuestionId());

            } catch (Exception e) {
                logger.error("invoke askService failed! method :[getQuestionById]");
            }
            if (question == null) {
                result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
                result.getNotification().setNotifInfo("当前问题不存在或已删除");
                return result;
            }
            try {
                updateQuestion(question, answer);

            } catch (Exception e) {
                logger.error("invoke askService failed! update question");
            }
            // 发送 通知
            if (user.getId() != praise.getAnswerId()) {
                MessageNotify message = createMessageNotify(praise, dbUser);
                dataSyncTask.saveDataNeedSync(new DataSync(0l, message));
            } else {
                logger.info("praise self answer! so skip send message");
            }
        }
        return result;
    }

    /**
     * 取消点赞
     * @param request
     * @param answerId
     * @return
     */
    @RequestMapping(value = "/{answerId}", method = RequestMethod.DELETE)
    public InterfaceResult delete(HttpServletRequest request, @PathVariable long answerId) {

        InterfaceResult result = null;
        User user = this.getUser(request);
        List<PartPraise> remList = new ArrayList<PartPraise>(3);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        long userId = user.getId();
        try {
            result = praiseService.delete(answerId, userId);
            if ("0".equals(result.getNotification().getNotifCode())) {
                // 删除 redis 中 数据
                this.removePraiseUId(answerId, userId);
                Answer answer = answerService.getAnswerById(answerId);
                List<PartPraise> partPraiseList = answer.getPartPraiseList();
                if (CollectionUtils.isNotEmpty(partPraiseList)) {
                    for (PartPraise partPraise : partPraiseList) {
                        if (partPraise == null)
                            continue;
                        if (partPraise.getAdmirerId() == userId) {
                            remList.add(partPraise);
                        }
                    }
                }
                partPraiseList.removeAll(remList);
                List<Praise> praiseList = praiseService.getPartPraiseUser(answerId, 0, 3);
                if (CollectionUtils.isNotEmpty(praiseList)) {
                    PartPraise part = new PartPraise();
                    for (Praise praise : praiseList) {
                        if (praise == null)
                            continue;
                        convertPartPraise(praise, partPraiseList, part);
                    }
                }
                answer.setPartPraiseList(partPraiseList);
                // 修改 答案表
                updateAnswerResult(answer);
            }
        } catch (Exception e) {
            logger.error("invoke praise service failed! please check service" + e.getMessage());
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        return result;
    }

    /**
     * 点赞 过的人 列表
     * @param request
     * @param answerId
     * @return
     */
    @RequestMapping(value = "/{answerId}/{start}/{size}", method = RequestMethod.GET)
    public InterfaceResult getPraiseUser(HttpServletRequest request, @PathVariable long answerId,
                                         @PathVariable int start, @PathVariable int size) {

        InterfaceResult result = null;
        User user = this.getUser(request);
        if (user == null)
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        try {
            List<Praise> praiseList = praiseService.getPraiseUser(answerId, start, size);
            praiseList = convertPraiseList(praiseList);
            result = InterfaceResult.getSuccessInterfaceResultInstance(praiseList);
        } catch (Exception e) {
            logger.error("invoke praise service failed! method : [ getPraiseUser ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return result;
    }

    private MessageNotify createMessageNotify(Praise praise, User user) {

        if (praise == null) {
            logger.error("praise is null! so skip createMessage!");
            return null;
        }
        MessageNotify message = new MessageNotify();
        message.setTitle("赞了你的回答");
        message.setFromId(user.getId());
        message.setFromName(user.getName());
        message.setPicPath(user.getPicPath());
        //message.setType(MessageNotifyType.E);
        message.setType(17);
        message.setToId(praise.getAnswererId());
        message.setContent(convertToJson(praise.getAnswerId()));
        final short virtual = user.isVirtual() ? (short) 1 : (short) 0;
        message.setVirtual(virtual);

        return message;

    }

    private String convertToJson(long answerId) {

        Map<String, Object> map = mapContent(answerId);
        return AskAnswerJsonUtils.writeObjectToJson(map);
    }

    private Map<String, Object> mapContent(long questionId) {

        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("id", questionId);
        //map.put("type", MessageNotifyType.EKnowledge.value());
        map.put("type", 17);
        return map;
    }

    private void updateAnswer(User user, Answer answer) throws Exception{

        if (answer != null) {

            List<PartPraise> partPraiseList = answer.getPartPraiseList();
            if (CollectionUtils.isEmpty(partPraiseList)) {
                partPraiseList = new ArrayList<PartPraise>(3);
            }
            // 重新 设置 praiseCount 通过 redis 得到 点赞者的 size
            Set<String> set = this.getPraiseUIdSet(answer.getId());
            answer.setPraiseCount(set.size());
            if (partPraiseList.size() < 3) {
                PartPraise partPraise = new PartPraise();
                convertPartPraiseByUser(user, partPraiseList, partPraise);
            }
            answer.setPartPraiseList(partPraiseList);
            updateAnswerResult(answer);
        }
    }

    private void updateQuestion(Question question, Answer answer) {

        PartAnswer topAnswer = null;
        try {
            topAnswer = question.getTopAnswer();
            if (topAnswer == null) {
                topAnswer = convertAnswer(answer);
                question.setTopAnswer(topAnswer);
                updateTopQuestion(question);
            } else {
                byte top = topAnswer.getTop();
                // 最优答案 是非置顶的情况
                if (top == 0) {
                    int praiseCount = topAnswer.getPraiseCount();
                    if (answer.getPraiseCount() > praiseCount) {
                        topAnswer = convertAnswer(answer);
                        question.setTopAnswer(topAnswer);
                        updateTopQuestion(question);
                    }
                } else {
                // 最优答案 是置顶的不进行修改
                }
            }
        } catch (Exception e) {
            logger.error("invoke ask service failed! method: [updateQuestion]");
        }
    }

    private void updateTopQuestion(Question question) throws Exception{

        // 修改问题 最优答案
        InterfaceResult result = askService.updateQuestion(question);
        if (!"0".equals(result.getNotification().getNotifCode())) {
            logger.error("update question failed! please check askService, check provider log");
        }
    }

    private List<Praise> convertPraiseList(List<Praise> praiseList) {

        if (CollectionUtils.isNotEmpty(praiseList)) {
            for (Praise praise : praiseList) {
                if (praise == null)
                    continue;
                long admirerId = praise.getAdmirerId();
                User user = userService.selectByPrimaryKey(admirerId);
                praise.setAdmirerName(user.getName());
                praise.setAdmirerPicPath(user.getPicPath());
            }
        }
        return praiseList;
    }

    private void updateAnswerResult(Answer answer) {

        InterfaceResult result = answerService.updateAnswer(answer);
        if (!"0".equals(result.getNotification().getNotifCode())) {
            logger.error("update answer failed! please check answerService ,check provider log");
        }
    }

    private void convertPartPraise(Praise praise, List<PartPraise> partPraiseList, PartPraise part) {

        PartPraise partPraise = part;
        long admirerId = praise.getAdmirerId();
        User admireUser = userService.selectByPrimaryKey(admirerId);
        partPraise.setAdmirerId(admirerId);
        partPraise.setAdmirerName(admireUser.getName());
        partPraise.setAdmirerPicPath(admireUser.getPicPath());
        final short virtual = admireUser.isVirtual() ? (short) 1 : (short) 0;
        partPraise.setVirtual(virtual);
        partPraiseList.add(partPraise);
    }

    private void convertPartPraiseByUser(User user, List<PartPraise> partPraiseList, PartPraise part) {

        PartPraise partPraise = part;
        partPraise.setAdmirerId(user.getId());
        partPraise.setAdmirerName(user.getName());
        partPraise.setAdmirerPicPath(user.getPicPath());
        final short virtual = user.isVirtual() ? (short) 1 : (short) 0;
        partPraise.setVirtual(virtual);
        partPraiseList.add(partPraise);
    }
}
