package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.util.JsonUtils;
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
import com.gintong.ywxt.im.model.MessageNotifyType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            // 更新答案表
            try {
                updateAnswer(dbUser, answer);

            } catch (Exception e) {
                logger.error("invoke answer service failed! method :[ getAnswerById, updateAnswer ]");
            }
            // 更新问题表
            //updateQuestion()
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
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        try {
            result = praiseService.delete(answerId, user.getId());

        } catch (Exception e) {
            logger.error("invoke praise service failed! please check service");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
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
            if (partPraiseList.size() < 3) {
                PartPraise partPraise = new PartPraise();
                partPraise.setAdmirerId(user.getId());
                partPraise.setAdmirerName(user.getName());
                partPraise.setAdmirerPicPath(user.getPicPath());
                final short virtual = user.isVirtual() ? (short) 1 : (short) 0;
                partPraise.setVirtual(virtual);
                partPraiseList.add(partPraise);
                InterfaceResult result = answerService.updateAnswer(answer);
                if (!"0".equals(result.getNotification().getNotifCode())) {
                    logger.error("update answer failed! please check answerService ,check provider log");
                }
            }
        }
    }

    private void updateQuestion(Question question, Answer answer) {

        List<PartAnswer> partAnswerList = null;
        List<PartAnswer> removeList = new ArrayList<PartAnswer>(1);
        List<PartAnswer> addList = new ArrayList<PartAnswer>(1);
        try {
            partAnswerList = question.getTopAnswerList();
            if (CollectionUtils.isEmpty(partAnswerList)) {
                partAnswerList = new ArrayList<PartAnswer>();
                PartAnswer partAnswer = convertAnswer(answer);
                partAnswerList.add(partAnswer);
            } else {
                for (PartAnswer partAnswer : partAnswerList) {
                    if (partAnswer == null)
                        continue;
                    byte top = partAnswer.getTop();
                    // 最优答案 是非置顶的情况
                    if (top == 0) {
                        int praiseCount = partAnswer.getPraiseCount();
                        if (answer.getPraiseCount() > praiseCount) {

                            removeList.add(partAnswer);
                            PartAnswer addPartAnswer = convertAnswer(answer);
                            addList.add(addPartAnswer);
                        }
                    } else {
                    // 最优答案 是置顶的不进行修改
                    }
                }
                partAnswerList.removeAll(removeList);
                partAnswerList.addAll(addList);
            }
            question.setTopAnswerList(partAnswerList);
            for (PartAnswer topAnswer : question.getTopAnswerList()) {
                if (topAnswer.getTop() == 0) {
                    // 这种情况 只有 一个点赞数最多的 topAnswer 数据 ，所以遍历 list 进行修改操作，不会太影响性能
                    // 修改问题 最优答案
                    InterfaceResult result = askService.updateQuestion(question);
                    if (!"0".equals(result.getNotification().getNotifCode())) {
                        logger.error("update question failed! please check askService, check provider log");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("invoke ask service failed! method: [updateQuestion]");
        }
    }

    private PartAnswer convertAnswer(Answer answer) {

        PartAnswer partAnswer = new PartAnswer();
        partAnswer.setAnswererId(answer.getAnswererId());
        partAnswer.setAnswerId(answer.getId());
        partAnswer.setContent(answer.getContent());
        partAnswer.setPraiseCount(answer.getPraiseCount());
        partAnswer.setType(answer.getType());
        partAnswer.setVirtual(answer.getVirtual());
        return partAnswer;
    }

    private void updateTopQuestion(List<PartAnswer> partAnswerList, PartAnswer partAnswer, Question question) {

        partAnswerList.add(partAnswer);
        question.setTopAnswerList(partAnswerList);
        // 修改问题 最优答案
        InterfaceResult result = askService.updateQuestion(question);
        if (!"0".equals(result.getNotification().getNotifCode())) {
            logger.error("update question failed! please check askService, check provider log");
        }
    }
}
