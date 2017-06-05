package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.interlocution.model.Answer;
import com.ginkgocap.ywxt.interlocution.model.ID;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import com.ginkgocap.ywxt.interlocution.web.service.AnswerServiceLocal;
import com.ginkgocap.ywxt.user.model.User;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Wang fei on 2017/6/5.
 */
@RestController
@RequestMapping("/other")
public class AskAnswerOtherController extends BaseController{

    private final Logger logger = LoggerFactory.getLogger(AskAnswerOtherController.class);

    @Resource
    private AskService askService;

    @Resource
    private AnswerService answerService;

    @Resource
    private AnswerServiceLocal answerServiceLocal;

    /**
     * 置顶 问题/答案
     * @param request
     * @param topType 0：问题 1：答案
     * @return
     */
    @RequestMapping(value = "/top/{topType}", method = RequestMethod.POST)
    public InterfaceResult addQuestionTop(HttpServletRequest request, @PathVariable byte topType) {

        User user = this.getYINUser(request);
        InterfaceResult result = null;
        long id = 0;
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        InterfaceResult idResult = getId(request);
        if ("0".equals(idResult.getNotification().getNotifCode())) {
            id = (Long)idResult.getResponseData();
        }
        if (topType == 0) {
            try {
                //result = askService.addTop(id);
            } catch (Exception e) {
                logger.error("invoke ask service failed! method :[ addTop ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        } else {
            result = answerServiceLocal.addTop(id);
        }
        return result;
    }

    /**
     * 取消 置顶
     * @param request
     * @param topType 0：问题 1：答案
     * @return
     */
    @RequestMapping(value = "/top/{topType}", method = RequestMethod.DELETE)
    public InterfaceResult deleteTop(HttpServletRequest request, @PathVariable byte topType) {

        InterfaceResult result = null;
        long id = 0;
        User user = this.getYINUser(request);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        InterfaceResult idResult = getId(request);
        if ("0".equals(idResult.getNotification().getNotifCode())) {
            id = (Long)idResult.getResponseData();
        }
        if (topType == 0) {
            try {
                //result = askService.deleteTop(id);
            } catch (Exception e) {
                logger.error("invoke ask service failed! method :[ deleteTop ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        } else {
            try {
                result = answerServiceLocal.deleteTop(id);
            } catch (Exception e) {
                logger.error("invoke answer service failed! method :[ deleteTop ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        }
        return result;
    }

    private InterfaceResult getId(HttpServletRequest request) {

        InterfaceResult result = null;
        String requestJson = null;
        ID id = null;
        try {
            requestJson = this.getBodyParam(request);
            id = (ID)JsonUtils.jsonToBean(requestJson, ID.class);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        if (id != null && id.getId() > 0) {
            result = InterfaceResult.getSuccessInterfaceResultInstance(id.getId());
        }
        return result;
    }
}
