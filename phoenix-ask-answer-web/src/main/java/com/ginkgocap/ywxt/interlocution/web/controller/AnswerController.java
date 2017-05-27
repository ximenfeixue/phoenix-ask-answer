package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.interlocution.model.Answer;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.ginkgocap.ywxt.user.model.User;
import com.ginkgocap.ywxt.user.service.UserService;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

        // WILL do send message
        return result;
    }

}
