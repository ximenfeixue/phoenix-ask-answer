package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.associate.model.Associate;
import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.interlocution.model.*;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import com.ginkgocap.ywxt.interlocution.web.service.AskServiceLocal;
import com.ginkgocap.ywxt.interlocution.web.service.AssociateServiceLocal;
import com.ginkgocap.ywxt.user.model.User;
import com.ginkgocap.ywxt.user.service.UserService;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * Created by wang fei on 2017/5/23.
 */
@RestController
@RequestMapping("/ask")
public class AskController extends BaseController{

    private final Logger logger = LoggerFactory.getLogger(AskController.class);

    @Resource
    private AskService askService;

    @Resource
    private AssociateServiceLocal associateServiceLocal;

    @Resource
    private UserService userService;

    @Resource
    private AskServiceLocal askServiceLocal;

    @RequestMapping(method = RequestMethod.POST)
    public InterfaceResult create(HttpServletRequest request, HttpServletResponse response) {

        InterfaceResult result = null;
        String requestJson = null;
        DataBase base = null;
        User user = getUser(request);
        logger.info("userID:" + user.getId() + "userName:" + user.getName() + "picPath:" + user.getPicPath());
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        try {
            requestJson = this.getBodyParam(request);
            logger.info("requestJson:" + requestJson);
            base = (DataBase)JsonUtils.jsonToBean(requestJson, DataBase.class);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        result = this.create(base, user);
        long questionId = Long.valueOf(result.getResponseData().toString());

        /** save asso **/
        List<Associate> associateList = base.getAssociateList();
        if (CollectionUtils.isNotEmpty(associateList)) {
            List<Associate> saveAssociateList = associateServiceLocal.createAssociate(associateList, questionId, user);
            if (CollectionUtils.isEmpty(saveAssociateList)) {
                logger.error("insert asso failed! please check associate service!");
                // 并不是主要逻辑， 不抛出异常
                //return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
            }
        }
        return result;
    }

    /**
     * 主页 发现 - 回答
     * @param request
     * @param start 索引 0, 1, 2 ...
     * @param size 条数
     * @param status 回答状态 0 ：未回答 1：已回答 -1: 全部
     * @return
     */
    @RequestMapping(value = "/all/{status}/{start}/{size}", method = RequestMethod.GET)
    public InterfaceResult getAllAskAnswer(HttpServletRequest request, @PathVariable byte status,
                                           @PathVariable int start, @PathVariable int size) {
        User user = this.getJTNUser(request);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        if (start < 0 || size <= 0 || status > 1) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        List<Question> questionList = null;
        try {
            questionList = askService.getAllAskAnswerByStatus(status, start, size);
            questionList = convertList(questionList);
        } catch (Exception e) {
            logger.error("invoke getAllAskAnswerByStatus method failed !" + e.getMessage());
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(questionList);
    }

    /**
     * 问题 详情
     * @param request
     * @param questionId 问题 id
     * @param start 答案 索引 0, 1, 2 ...
     * @param size 答案 个数
     * @return
     */
    @RequestMapping(value = "/{questionId}/{start}/{size}", method = RequestMethod.GET)
    public MappingJacksonValue detail(HttpServletRequest request, @PathVariable long questionId,
                                      @PathVariable int start, @PathVariable int size) {

        MappingJacksonValue jacksonValue = null;
        InterfaceResult result = null;
        QuestionBase base = null;
        User user = this.getJTNUser(request);
        if (start < 0 || size <= 0) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            jacksonValue = new MappingJacksonValue(result);
            return jacksonValue;
        }
        try {
            base = askServiceLocal.getQuestionById(questionId, start, size);
        } catch (Exception e) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            jacksonValue = new MappingJacksonValue(result);
            return jacksonValue;
        }
        if (base == null || base.getQuestion() == null) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
            result.getNotification().setNotifInfo("当前问题不存在或已删除！");
            jacksonValue = new MappingJacksonValue(result);
            return jacksonValue;
        }
        Question question = base.getQuestion();
        convertQuestionUser(question);
        List<Answer> answerList = base.getAnswerList();
        if (CollectionUtils.isNotEmpty(answerList)) {
            convertAnswerUserList(answerList);
        }
        result = InterfaceResult.getSuccessInterfaceResultInstance(base);
        jacksonValue = new MappingJacksonValue(result);
        jacksonValue.setFilters(this.assoFilterProvider(Associate.class.getName()));
        return jacksonValue;
    }

    //@RequestMapping(value = "")

    private InterfaceResult create(DataBase base, User user) {

        InterfaceResult result = null;
        Question question = base.getQuestion();
        question.setUserId(user.getId());
        //question.setUserName(user.getName());
        //question.setPicPath(user.getPicPath());
        try {
            result = askService.insert(question);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        return result;
    }

    /**
     * 将发现 问答中 user 的 name 和 picPath 补全
     * @param questionList
     * @return
     */
    private List<Question> convertList(List<Question> questionList) {

        for (Question question : questionList) {
            long userId = question.getUserId();
            User user = userService.selectByPrimaryKey(userId);
            question.setUserName(user.getName());
            question.setPicPath(user.getPicPath());
            PartAnswer topAnswer = question.getTopAnswer();
            if (topAnswer != null && topAnswer.getAnswererId() != 0) {
                long answererId = topAnswer.getAnswererId();
                User answerer = userService.selectByPrimaryKey(answererId);
                if (answerer == null) {
                    logger.error("invoke userService failed ! method selectByPrimaryKey , userId : [" + answererId + "]");
                } else {
                    topAnswer.setAnswererName(answerer.getName());
                    topAnswer.setAnswererPicPath(answerer.getPicPath());
                }
            }
        }
        return questionList;
    }

    /**
     *
     * 将提出问题 user 的 name 和 picPath 补全
     *
     * @param question
     */
    private void convertQuestionUser(Question question) {

        long userId = question.getUserId();
        User user = userService.selectByPrimaryKey(userId);
        question.setUserName(user.getName());
        question.setPicPath(user.getPicPath());
    }

    /**
     *
     * 将答案 list 中 回答者 name 和 picPath 补全
     *
     * ｛ 若拿到的 list 数据 有异常， 重新 new list
     * 将 answerList 放到 新的 list 再返回 ｝
     *
     * @param answerList
     */
    private List<Answer> convertAnswerUserList(List<Answer> answerList) {

        for (Answer answer : answerList) {
            if (answer == null)
                continue;
            long answererId = answer.getAnswererId();
            User user = userService.selectByPrimaryKey(answererId);
            answer.setAnswererName(user.getName());
            answer.setAnswererPicPath(user.getPicPath());
        }
        return answerList;
    }
}
