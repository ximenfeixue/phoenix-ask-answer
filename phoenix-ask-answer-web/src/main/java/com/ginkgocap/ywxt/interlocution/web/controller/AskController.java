package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.associate.model.Associate;
import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.cache.Cache;
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
import java.util.Set;


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

    @Resource
    private Cache cache;

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
            questionList = convertList(questionList, user.getId());
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
        long userId = user.getId();
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
        convertQuestionUser(question, userId);
        List<Answer> answerList = base.getAnswerList();
        if (CollectionUtils.isNotEmpty(answerList)) {
            convertAnswerUserList(answerList, userId);
        }
        result = InterfaceResult.getSuccessInterfaceResultInstance(base);
        jacksonValue = new MappingJacksonValue(result);
        jacksonValue.setFilters(this.assoFilterProvider(Associate.class.getName()));
        return jacksonValue;
    }

    /**
     * 我的问答
     * @param request
     * @param askAnswerType -1：全部问答 0：我提出的 1：我回答的 2：我收藏的
     * @param start
     * @param size
     * @return
     */
    @RequestMapping(value = "/my/{askAnswerType}/{start}/{size}", method = RequestMethod.GET)
    public InterfaceResult getMyQuestion(HttpServletRequest request, @PathVariable byte askAnswerType,
                                         @PathVariable int start, @PathVariable int size) {

        InterfaceResult result = null;
        User user = this.getUser(request);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        long userId = user.getId();
        List<Question> questionList = null;
        List<QuestionHome> questionHomeList = null;
        List<QuestionCollect> questionCollectList = null;
        if (askAnswerType == 0) {
            try {
                questionList = askService.getQuestionByUId(userId, start, size);
                questionList = convertList(questionList, userId);
                result = InterfaceResult.getSuccessInterfaceResultInstance(questionList);
            } catch (Exception e) {
                logger.error("invoke ask service failed! method : [getQuestionByUId]" + "userId:" + userId);
                result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        } else if (askAnswerType == 1) {
            try {
                questionHomeList = askServiceLocal.getAnswerByUId(userId, start, size);
                questionHomeList = convertMyAnswerList(questionHomeList, user);
                result = InterfaceResult.getSuccessInterfaceResultInstance(questionHomeList);
            } catch (Exception e) {
                logger.error("invoke ask service local failed! method : [ getAnswerByUId ]");
                result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        } else if (askAnswerType == 2) {
            try {
                questionCollectList = askService.getCollectByUId(userId, start, size);
                questionCollectList = convertMyCollectList(questionCollectList);
                result = InterfaceResult.getSuccessInterfaceResultInstance(questionCollectList);
            } catch (Exception e) {
                logger.error("invoke ask service failed! method : [ getCollectByUId ]");
                result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        } else {
            /*try {
                //暂时不做 全部 问答
            } catch (Exception e) {
                logger.error("");
                result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }*/
        }
        return result;
    }

    private InterfaceResult create(DataBase base, User user) {

        InterfaceResult result = null;
        Question question = base.getQuestion();
        question.setUserId(user.getId());
        final short virtual = user.isVirtual() ? (short) 1 : (short) 0;
        question.setVirtual(virtual);
        try {
            result = askService.insert(question);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        return result;
    }

    /**
     * 将发现 问答中/ 我的提问中 user 的 name 和 picPath 补全
     *
     * 以后系统 优化 将 user 的 name picPath 都放到 redis 中
     *
     * 业务逻辑 导致 遍历 中操作 数据库 可优化最好了
     * @param questionList
     * @return
     */
    private List<Question> convertList(List<Question> questionList, long currentUserId) {

        for (Question question : questionList) {
            long userId = question.getUserId();
            User user = userService.selectByPrimaryKey(userId);
            question.setUserName(user.getName());
            question.setPicPath(user.getPicPath());
            PartAnswer topAnswer = question.getTopAnswer();
            if (topAnswer != null) {
                long answererId = topAnswer.getAnswererId();
                long answerId = topAnswer.getAnswerId();
                User answerer = userService.selectByPrimaryKey(answererId);
                if (answerer == null) {
                    logger.error("invoke userService failed ! method selectByPrimaryKey , userId : [" + answererId + "]");
                } else {
                    topAnswer.setAnswererName(answerer.getName());
                    topAnswer.setAnswererPicPath(answerer.getPicPath());
                    boolean existPraise = this.isExistPraise(answerId, currentUserId);
                    topAnswer.setIsPraise((byte)(existPraise ? 1 : 0));
                    Set<String> praiseUIdSet = this.getPraiseUIdSet(answerId);
                    topAnswer.setPraiseCount(praiseUIdSet.size());
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
    private void convertQuestionUser(Question question, long currentUId) {

        long userId = question.getUserId();
        long id = question.getId();
        User user = userService.selectByPrimaryKey(userId);
        question.setUserName(user.getName());
        question.setPicPath(user.getPicPath());
        QuestionCollect collect = null;
        try {
            collect = askService.getCollectByUIdQuestionId(currentUId, id);
        } catch (Exception e) {
            logger.error("invoke ask service failed! method: [ getCollectByUIdQuestionId ]");
        }
        question.setIsCollect((byte)(collect == null ? 0 : 1));
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
    private List<Answer> convertAnswerUserList(List<Answer> answerList, long currentUserId) {

        if (CollectionUtils.isNotEmpty(answerList)) {
            for (Answer answer : answerList) {
                if (answer == null)
                    continue;
                long answererId = answer.getAnswererId();
                long id = answer.getId();
                User user = userService.selectByPrimaryKey(answererId);
                answer.setAnswererName(user.getName());
                answer.setAnswererPicPath(user.getPicPath());
                boolean existPraise = this.isExistPraise(id, currentUserId);
                answer.setIsPraise((byte)(existPraise ? 1 : 0));
                Set<String> praiseUIdSet = this.getPraiseUIdSet(id);
                answer.setPraiseCount(praiseUIdSet.size());
            }
        }
        return answerList;
    }

    /**
     * 我的 回答 列表 补全 name 和 picPath
     * @param questionHomeList
     * @param user
     * @return
     */
    private List<QuestionHome> convertMyAnswerList(List<QuestionHome> questionHomeList, User user) {

        if (CollectionUtils.isNotEmpty(questionHomeList)) {
            for (QuestionHome questionHome : questionHomeList) {
                if (questionHome == null)
                    continue;
                Question question = questionHome.getQuestion();
                if (question ==  null)
                    continue;
                long userId = question.getUserId();
                User qUser = userService.selectByPrimaryKey(userId);
                question.setUserName(qUser.getName());
                question.setPicPath(qUser.getPicPath());
                Answer answer = questionHome.getAnswer();
                if (answer == null)
                    continue;
                long id = answer.getId();
                answer.setAnswererName(user.getName());
                answer.setAnswererPicPath(user.getPicPath());
                boolean existPraise = this.isExistPraise(id, user.getId());
                answer.setIsPraise((byte)(existPraise ? 1 : 0));
                Set<String> praiseUIdSet = this.getPraiseUIdSet(id);
                answer.setPraiseCount(praiseUIdSet.size());
            }
        }
        return questionHomeList;
    }

    private List<QuestionCollect> convertMyCollectList(List<QuestionCollect> collectList) {

        if (CollectionUtils.isNotEmpty(collectList)) {
            for (QuestionCollect collect : collectList) {
                if (collect == null)
                    continue;
                long ownerId = collect.getOwnerId();
                User user = userService.selectByPrimaryKey(ownerId);
                collect.setOwnerName(user.getName());
                collect.setOwnerPicPath(user.getPicPath());
            }
        }
        return collectList;
    }
}
