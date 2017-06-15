package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.interlocution.model.*;
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
                result = askService.addTop(id);
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
    @RequestMapping(value = "/top/{topType}/{id}", method = RequestMethod.DELETE)
    public InterfaceResult deleteTop(HttpServletRequest request, @PathVariable byte topType,
                                     @PathVariable long id) {

        InterfaceResult result = null;
        User user = this.getYINUser(request);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        if (topType == 0) {
            try {
                result = askService.deleteTop(id);
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

    /**
     * 收藏 问题
     * @param request
     * @return
     */
    @RequestMapping(value = "/collect", method = RequestMethod.POST)
    public InterfaceResult collect(HttpServletRequest request) {

        InterfaceResult result = null;
        String requestJson = null;
        QuestionCollect collect = null;
        User user = this.getUser(request);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        try {
            requestJson = this.getBodyParam(request);
            collect = (QuestionCollect)JsonUtils.jsonToBean(requestJson, QuestionCollect.class);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        collect.setUserId(user.getId());
        try {
            result = askService.addCollect(collect);
        } catch (Exception e) {
            logger.error("invoke ask service failed! method :[ addCollect ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        return result;
    }

    /**
     * 取消 收藏
     * @param request
     * @return
     */
    @RequestMapping(value = "/collect/{questionId}", method = RequestMethod.DELETE)
    public InterfaceResult deleteCollect(HttpServletRequest request, @PathVariable long questionId) {

        InterfaceResult result = null;
        User user = this.getUser(request);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        if (questionId < 0)
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_NULL_EXCEPTION);
        result = askService.deleteCollect(questionId, user.getId());
        return result;
    }

    /**
     * 举报 问题
     * @param request
     * @return
     */
    @RequestMapping(value = "/report", method = RequestMethod.POST)
    public InterfaceResult addReport(HttpServletRequest request) {

        InterfaceResult result = null;
        String requestJson = null;
        QuestionReport report = null;
        User user = this.getUser(request);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        try {
            requestJson = this.getBodyParam(request);
            report = (QuestionReport) JsonUtils.jsonToBean(requestJson, QuestionReport.class);
        } catch (Exception e) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        report.setUserId(user.getId());
        try {
            result = askService.addReport(report);
        } catch (Exception e) {
            logger.error("invoke ask service failed ! method :[ addReport ]");
        }
        return result;
    }

    /**
     * 删除 问答 (运营 后台)
     * @param request
     * @param deleteType 0：问题 1：答案
     * @param id 根据type 传 问题 id  答案 id
     * @return
     */
    @RequestMapping(value = "/{deleteType}/{id}", method = RequestMethod.DELETE)
    public InterfaceResult removeAskAnswer(HttpServletRequest request, @PathVariable byte deleteType,
                                           @PathVariable long id) {

        InterfaceResult result = null;
        User yinUser = this.getYINUser(request);
        if (yinUser == null)
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        if (deleteType == 0) {
            try {
                result = askService.deleteQuestion(id);
            } catch (Exception e) {
                logger.error("invoke ask service failed! method : [ deleteQuestion ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        } else {
             result = answerServiceLocal.removeAnswer(id);
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
            logger.info("ID class : [" + id.getId() + "]");
            result = InterfaceResult.getSuccessInterfaceResultInstance(id.getId());
        }
        return result;
    }
}
