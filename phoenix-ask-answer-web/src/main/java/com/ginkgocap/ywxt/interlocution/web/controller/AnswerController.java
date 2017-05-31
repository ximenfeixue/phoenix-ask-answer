package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.interlocution.model.Answer;
import com.ginkgocap.ywxt.interlocution.model.DataSync;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.ginkgocap.ywxt.interlocution.service.DataSyncService;
import com.ginkgocap.ywxt.interlocution.utils.AskAnswerJsonUtils;
import com.ginkgocap.ywxt.interlocution.utils.MyStringUtils;
import com.ginkgocap.ywxt.interlocution.web.Task.DataSyncTask;
import com.ginkgocap.ywxt.user.model.User;
import com.ginkgocap.ywxt.user.service.UserService;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import com.gintong.ywxt.im.model.MessageNotify;
import com.gintong.ywxt.im.model.MessageNotifyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

    /**
     * 创建 答案
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public InterfaceResult create(HttpServletRequest request) {

        String requestJson = null;
        Answer answer = null;
        Long id = null;
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
        answer.setAnswererId(user.getId());
        try {
            result = answerService.insert(answer);
        } catch (Exception e) {
            logger.error("invoke answerService failed : method :[ create ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        User dbUser = userService.selectByPrimaryKey(user.getId());
        // WILL do send message
        if (result.getResponseData() != null && (Long)result.getResponseData() > 0) {
            if (user.getId() != answer.getToId()) {
                MessageNotify message = createMessageNotify(answer, dbUser);
                dataSyncTask.saveDataNeedSync(new DataSync(0l, message));
            } else {
                logger.info("respond self question ! so skip create message!");
            }
        }
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
