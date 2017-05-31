package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.interlocution.model.Answer;
import com.ginkgocap.ywxt.interlocution.model.DataSync;
import com.ginkgocap.ywxt.interlocution.model.Praise;
import com.ginkgocap.ywxt.interlocution.service.PraiseService;
import com.ginkgocap.ywxt.interlocution.utils.AskAnswerJsonUtils;
import com.ginkgocap.ywxt.interlocution.web.Task.DataSyncTask;
import com.ginkgocap.ywxt.user.model.User;
import com.ginkgocap.ywxt.user.service.UserService;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import com.gintong.ywxt.im.model.MessageNotify;
import com.gintong.ywxt.im.model.MessageNotifyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
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

    /**
     * 创建点赞
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    public InterfaceResult create(HttpServletRequest request) {

        InterfaceResult result = null;
        Praise praise = null;
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
        try {
            result = praiseService.create(praise);

        } catch (Exception e) {
            logger.error("invoke praiseService failed : method :[ create ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        if (result.getResponseData() != null && (Long)result.getResponseData() > 0) {
            User dbUser = null;
            try {
                dbUser = userService.selectByPrimaryKey(user.getId());
            } catch (Exception e) {
                logger.error("invoke userService failed !please check userService");
            }
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
            result = praiseService.delete(answerId);

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
}
