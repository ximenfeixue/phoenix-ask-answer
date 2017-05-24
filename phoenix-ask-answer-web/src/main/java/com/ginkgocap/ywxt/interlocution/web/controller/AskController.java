package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.associate.model.Associate;
import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.interlocution.model.DataBase;
import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import com.ginkgocap.ywxt.interlocution.web.service.AssociateServiceLocal;
import com.ginkgocap.ywxt.user.model.User;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * Created by wang fei on 2017/5/23.
 */
@RestController
@RequestMapping("/interlocution")
public class AskController extends BaseController{

    private final Logger logger = LoggerFactory.getLogger(AskController.class);

    @Resource
    private AskService askService;

    @Resource
    private AssociateServiceLocal associateServiceLocal;

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public InterfaceResult create(HttpServletRequest request) {

        InterfaceResult result = null;
        String requestJson = null;
        DataBase base = null;
        User user = this.getUser(request);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        try {
            requestJson = this.getBodyParam(request);
            base = (DataBase)JsonUtils.jsonToBean(requestJson, DataBase.class);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        result = this.create(base, user);
        long questionId = Long.valueOf(result.getResponseData().toString());

        /** save asso **/
        List<Associate> associateList = base.getAsso();
        List<Associate> saveAssociateList = associateServiceLocal.createAssociate(associateList, questionId, user);
        if (CollectionUtils.isEmpty(saveAssociateList)) {
            logger.error("insert asso failed! please check associate service!");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return result;
    }

    private InterfaceResult create(DataBase base, User user) {

        InterfaceResult result = null;
        Question question = base.getQuestion();
        question.setUserId(user.getId());
        question.setUserName(user.getName());
        question.setPicPath(user.getPicPath());
        result = askService.insert(question);
        if (result == null || result.getNotification() == null || !"0".equals(result.getNotification().getNotifCode())) {
            logger.error("insert question failed!");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return result;
    }
}
