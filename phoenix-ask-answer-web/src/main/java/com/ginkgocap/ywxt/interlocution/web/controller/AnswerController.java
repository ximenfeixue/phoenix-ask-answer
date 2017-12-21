package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.interlocution.model.*;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import com.ginkgocap.ywxt.interlocution.service.DataSyncService;
import com.ginkgocap.ywxt.interlocution.utils.AskAnswerJsonUtils;
import com.ginkgocap.ywxt.interlocution.utils.MyStringUtils;
import com.ginkgocap.ywxt.interlocution.web.Task.DataSyncTask;
import com.ginkgocap.ywxt.interlocution.web.service.AnswerServiceLocal;
import com.ginkgocap.ywxt.track.entity.constant.BusinessModelEnum;
import com.ginkgocap.ywxt.track.entity.constant.ModelFunctionEnum;
import com.ginkgocap.ywxt.track.entity.util.BusinessTrackUtils;
import com.ginkgocap.ywxt.user.model.User;
import com.ginkgocap.ywxt.user.service.UserService;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import com.gintong.ywxt.im.model.MessageNotify;
import com.gintong.ywxt.im.model.MessageNotifyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Wang fei on 2017/5/27.
 */
@RestController
@RequestMapping("/answer")
public class AnswerController extends BaseController{

    private final Logger logger = LoggerFactory.getLogger(AnswerController.class);

    @Resource
    private AnswerService answerService;

    @Resource
    private UserService userService;

    @Resource
    private DataSyncTask dataSyncTask;

    @Resource
    private AskService askService;

    @Resource
    private AnswerServiceLocal answerServiceLocal;

    /**
     * 创建 答案
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public InterfaceResult create(HttpServletRequest request) {

        String requestJson = null;
        Answer answer = null;
        long questionId = 0;
        InterfaceResult result = null;
        User user = this.getUser(request);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }

        try {
            requestJson = this.getBodyParam(request);
            answer = (Answer) JsonUtils.jsonToBean(requestJson, Answer.class);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        // check question is only virtual ?
        questionId = answer.getQuestionId();
        if (questionId < 0) {
            logger.error("questionId error!");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        Question question = null;
        try {
            question = askService.getQuestionById(questionId);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        if (question == null) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            result.getNotification().setNotifInfo("该问题不存在或已删除");
            return result;
        }
        // 回答者类型
        User dbUser = null;
        try {
            dbUser = userService.selectByPrimaryKey(user.getId());
        } catch (Exception e) {
            logger.error("invoke userService failed !please check userService");
        }
        byte answererType = question.getAnswererType();
        if (answererType == 1 && dbUser.isVirtual()) {
            try {
                Answer virAnswer = answerService.getAnswerByQuestionAndAnswererId(questionId, dbUser.getId());
                if (virAnswer != null) {
                    result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
                    result.getNotification().setNotifInfo("该问题权限设置为只能由一个组织回答哦");
                    return result;
                }
            } catch (Exception e) {
                logger.error("invoke answerService failed! method :[ getAnswerByQuestionAndAnswererId ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        }
        // 补全 answer
        answer.setAnswererId(user.getId());
        final short virtual = user.isVirtual() ? (short) 1 : (short) 0;
        answer.setVirtual(virtual);
        answer.setQuestionTitle(question.getTitle());
        try {
            result = answerService.insert(answer);
        } catch (Exception e) {
            logger.error("invoke answerService failed : method :[ create ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }

        if (result.getResponseData() != null && (Long)result.getResponseData() > 0) {
            // TODO: send message
            if (user.getId() != answer.getToId()) {
                MessageNotify message = createMessageNotify(answer, dbUser);
                dataSyncTask.saveDataNeedSync(new DataSync(0l, message));
            } else {
                logger.info("respond self question ! so skip create message!");
            }
            // update question status and answerCount
            byte status = 1;
            int answerCount = (int)addAnswerCountByRedis(questionId);
            logger.info("answerCount" + answerCount + "key: ask_answer_answerCount_" + questionId);
            try {
                boolean flag = askService.updateStatusAndAnswerCount(questionId, status, answerCount);
                if (!flag) {
                    logger.error("update status = 1 failed! method :[ updateStatusAndAnswerCount ]");
                } else {
                    logger.info("update status success");
                }
            } catch (Exception e) {
                logger.error("invoke askService failed! method :[ updateStatusAndAnswerCount ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
            }
            // TODO: add track log
            BusinessTrackUtils.addTbBusinessTrackLog4AddOpt(logger, TRACK_LOGGER, BusinessModelEnum.BUSINESS_QUESTIONS_ANSWERS.getKey(),
                    questionId, ModelFunctionEnum.MODEL_FUNCTION_ADD_ANSWER.getKey(), request, user.getId(), user.getName());
        }
        return result;
    }

    /**
     * 答案详情 暂时不需要
     * @param request
     * @return
     */
    @RequestMapping(value = "/{id}/{start}/{size}", method = RequestMethod.GET)
    public InterfaceResult detail(HttpServletRequest request, @PathVariable long id,
                                  @PathVariable int start, @PathVariable int size) {

        InterfaceResult result = null;
        AnswerBase base = null;
        try {
            //result = answerServiceLocal.getAnswer(id, start, size);

        } catch (Exception e) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        if (result.getResponseData() == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return result;
    }

    /**
     * 删除 自己 的答案
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public InterfaceResult delete(HttpServletRequest request, @PathVariable long id) {

        InterfaceResult result;
        Answer answer = null;
        Question question = null;
        User user = this.getUser(request);
        if (user == null)
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        try {
            answer = answerService.getAnswerById(id);
        } catch (Exception e) {
            logger.error("invoke answer service failed! method : [ getAnswerById ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        if (answer == null) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            result.getNotification().setNotifInfo("当前答案不存在或已删除");
            return result;
        }
        long questionId = answer.getQuestionId();
        try {
            question = askService.getQuestionById(questionId);
        } catch (Exception e) {
            logger.error("invoke ask service failed! method : [ getQuestionById ]" + e.getMessage());
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        if (question == null) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            result.getNotification().setNotifInfo("当前问题不存在或已删除");
            return result;
        }
        try {
            result = answerService.deleteAnswer(id, user.getId());
            logger.info("delete answer success ! id : " + id + " userId : " + user.getId());
        } catch (Exception e) {
            logger.error("invoke answer service failed! method : [ deleteAnswer ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        // 删除 答案成功 后，检查 该答案 是否是 发现页 中 最优答案
        // 删除 答案 成功 后 所做操作
        answerServiceLocal.afterRemoveAnswer(result, question, id);
        return result;
    }

    private MessageNotify createMessageNotify(Answer answer, User user) {

        if (answer == null) {
            logger.error("answer is null! so skip createMessage!");
            return null;
        }
        MessageNotify message = new MessageNotify();
        message.setTitle("回答了你的问题");
        message.setFromId(user.getId());
        message.setFromName(user.getName());
        message.setPicPath(user.getPicPath());
        //message.setType(MessageNotifyType.EAskAnswer);
        message.setType(16);
        message.setToId(answer.getToId());
        message.setContent(convertToJson(answer.getQuestionId()));
        final short virtual = user.isVirtual() ? (short) 1 : (short) 0;
        message.setVirtual(virtual);

        return message;
    }

    private String convertToJson(long questionId) {

        Map<String, Object> map = mapContent(questionId);
        return AskAnswerJsonUtils.writeObjectToJson(map);
    }

    private Map<String, Object> mapContent(long questionId) {

        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("id", questionId);
        //map.put("type", MessageNotifyType.EKnowledge.value());
        map.put("type", 16);
        return map;
    }

}
