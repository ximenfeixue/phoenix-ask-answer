package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.associate.model.Associate;
import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.cache.Cache;
import com.ginkgocap.ywxt.interlocution.model.*;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import com.ginkgocap.ywxt.interlocution.utils.AskAnswerJsonUtils;
import com.ginkgocap.ywxt.interlocution.web.Task.DataSyncTask;
import com.ginkgocap.ywxt.interlocution.web.service.AskServiceLocal;
import com.ginkgocap.ywxt.interlocution.web.service.AssociateServiceLocal;
import com.ginkgocap.ywxt.user.model.User;
import com.ginkgocap.ywxt.user.service.UserService;
import com.gintong.frame.util.Page;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import com.gintong.ywxt.im.model.MessageNotify;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


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

    @Resource
    private DataSyncTask dataSyncTask;

    @Resource
    private AnswerService answerService;

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
        if ("0".equals(result.getNotification().getNotifCode())) {
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
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            result.getNotification().setNotifInfo("当前问题不存在或已删除！");
            jacksonValue = new MappingJacksonValue(result);
            return jacksonValue;
        }
        Question question = base.getQuestion();
        setReadCountByRedis(question);
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
        long count = 0;
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
            } catch (Exception e) {
                logger.error("invoke ask service failed! method : [getQuestionByUId]" + "userId:" + userId);
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
            if (isWeb(request)) {
                try {
                    count = askService.countQuestionByUId(userId);
                } catch (Exception e) {
                    logger.error("invoke ask service failed! method : [ countQuestionByUId ]. userId : " + userId);
                    return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
                }
                result = convertMyQuestionPage(questionList, count, start, size);
            } else {
                result = InterfaceResult.getSuccessInterfaceResultInstance(questionList);
            }
        } else if (askAnswerType == 1) {
            try {
                questionHomeList = askServiceLocal.getAnswerByUId(userId, start, size);
                questionHomeList = convertMyAnswerList(questionHomeList, user, null);
            } catch (Exception e) {
                logger.error("invoke ask service local failed! method : [ getAnswerByUId ] userId : " + userId);
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
            if (isWeb(request)) {
                try {
                    count = answerService.countAnswerByUId(userId);
                } catch (Exception e) {
                    logger.error("invoke answer service failed! method : [ countAnswerByUId ] userId : " + userId);
                }
                result = convertMyAnswerPage(questionHomeList, count, start, size);
            } else {
                result = InterfaceResult.getSuccessInterfaceResultInstance(questionHomeList);
            }
        } else if (askAnswerType == 2) {
            try {
                questionCollectList = askService.getCollectByUId(userId, start, size);
                questionCollectList = convertMyCollectList(questionCollectList);
            } catch (Exception e) {
                logger.error("invoke ask service failed! method : [ getCollectByUId ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
            if (isWeb(request)) {
                try {
                    count = askService.countQuestionCollectByUId(userId);
                } catch (Exception e) {
                    logger.error("invoke ask service failed! method : [ countQuestionCollectByUId ]  userId : " + userId);
                    return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
                }
                result = convertMyCollectQuestionPage(questionCollectList, count, start, size);
            } else {
                result = InterfaceResult.getSuccessInterfaceResultInstance(questionCollectList);
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

    /**
     * 查询 其他人 问答
     * @param request
     * @param askAnswerType 0:其他人的提问 1：其他人的回答 2：其他人的收藏
     * @param start 第 0，1，2...页
     * @param size  每页显示条数
     * @param userId 要查询 userId
     * @return
     */
    @RequestMapping(value = "/other/{askAnswerType}/{start}/{size}/{userId}", method = RequestMethod.GET)
    public InterfaceResult getOtherQuestion(HttpServletRequest request, @PathVariable byte askAnswerType,
                                            @PathVariable int start, @PathVariable int size,
                                            @PathVariable long userId) {

        InterfaceResult result = null;
        long count = 0;
        User user = this.getJTNUser(request);
        if (user == null) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        }
        List<QuestionHome> questionHomeList = null;
        if (askAnswerType == 1) {
            try {
                User otherUser = userService.selectByPrimaryKey(userId);
                if (otherUser == null) {
                    return InterfaceResult.getSuccessInterfaceResultInstance(result);
                }
                questionHomeList = askServiceLocal.getAnswerByUId(userId, start, size);
                questionHomeList = convertMyAnswerList(questionHomeList, otherUser, user);
            } catch (Exception e) {
                logger.error("invoke ask service local failed! method : [ getAnswerByUId ] userId : " + userId);
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
            try {
                count = answerService.countAnswerByUId(userId);
            } catch (Exception e) {
                logger.error("invoke answer service failed! method : [ countAnswerByUId ] userId : " + userId);
            }
            result = convertMyAnswerPage(questionHomeList, count, start, size);
        }
        return result;
    }

    /**
     * 删除 问题
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public InterfaceResult delete(HttpServletRequest request, @PathVariable long id) {

        InterfaceResult result = null;
        Question question = null;
        User user = this.getUser(request);
        if (user == null)
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        try {
            question = askService.getQuestionById(id);
        } catch (Exception e) {
            logger.error("invoke ask service failed! method : [ getQuestionById ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        if (question == null) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            result.getNotification().setNotifInfo("当前问题不存在或已删除");
            return result;
        }
        String title = question.getTitle();
        try {
            logger.info("delete question id = " + id + " userId : " + user.getId());
            result = askService.deleteQuestion(id, user.getId());
        } catch (Exception e) {
            logger.error("invoke ask service failed! method [ deleteQuestion ] id:" + id);
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        // 删除 问题 成功 所做操作
        if ("0".equals(result.getNotification().getNotifCode()) && (Boolean)result.getResponseData()) {
            List<DataSync> dataList = new ArrayList<DataSync>();
            List<Answer> answerList = null;
            try {
                answerList = answerService.getAnswerListByQuestionId(id, -1, -1);
            } catch (Exception e) {
                logger.error("invoke answer service failed! method : [ getAnswerListByQuestionId ]");
            }
            if (CollectionUtils.isNotEmpty(answerList)) {
                for (Answer answer : answerList) {
                    MessageNotify message = createMessageNotify(title, answer, user);
                    DataSync dataSync = new DataSync(0l, message);
                    dataList.add(dataSync);
                }
                dataSyncTask.batchSaveDataNeedSync(dataList);
                // 批量修改 答案 状态
                boolean flag = false;
                try {
                    flag = answerService.batchUpdateAnswerStatus(id);
                } catch (Exception e) {
                    logger.error("invoke answer service failed! method : [ batchUpdateAnswerStatus ]");
                }
                if (!flag) {
                    logger.error("batch update answer status failed!");
                }
            }
        }
        return result;
    }

    /**
     * 修改 问题
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public InterfaceResult update(HttpServletRequest request, @PathVariable long id) {

        InterfaceResult result;
        String requestJson = null;
        DataBase base = null;
        Question dbQuestion = null;
        User user = this.getUser(request);
        if (user == null)
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        try {
            requestJson = this.getBodyParam(request);
            base = (DataBase) JsonUtils.jsonToBean(requestJson, DataBase.class);
        } catch (Exception e) {
            logger.error("param is null or error");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        try {
            dbQuestion = askService.getQuestionById(id);
        } catch (Exception e) {
            logger.error("invoke ask service failed! method : [ getQuestionById ]" + e.getMessage());
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        result = updateQuestion(base, dbQuestion);
        if ("0".equals(result.getNotification().getNotifCode())) {
            // 修改 关联
            /** update asso **/
            InterfaceResult updateResult = associateServiceLocal.updateAssociate(dbQuestion.getId(), user, base);
            if (!"0".equals(updateResult.getNotification().getNotifCode())) {
                logger.error("update asso failed! please check associate service!");
                // 并不是主要逻辑， 不抛出异常
                //return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
            }
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

    private InterfaceResult updateQuestion(DataBase base, Question dbQuestion) {

        InterfaceResult result;
        Question question = base.getQuestion();
        if (question == null)
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        convertBodyQuestionToDBQuestion(dbQuestion, question);
        try {
            result = askService.updateQuestion(dbQuestion);
        } catch (Exception e) {
            logger.error("invoke ask service failed! method : [ updateQuestion ]" + e.getMessage());
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

        if (CollectionUtils.isNotEmpty(questionList)) {
            for (Question question : questionList) {
                long userId = question.getUserId();
                User user = null;
                try {
                    user = userService.getUserById(userId);
                } catch (Exception e) {
                    logger.error("invoke userService error. userId :{}", userId);
                }
                if (user == null)
                    continue;
                question.setUserName(user.getName());
                question.setPicPath(user.getPicPath());
                question.setVirtual(user.isVirtual() ? (short) 1 : (short) 0);
                PartAnswer topAnswer = question.getTopAnswer();
                if (topAnswer != null) {
                    long answererId = topAnswer.getAnswererId();
                    long answerId = topAnswer.getAnswerId();
                    User answerer = null;
                    try {
                        answerer = userService.getUserById(userId);
                    } catch (Exception e) {
                        logger.error("invoke userService error. userId :{}", userId);
                    }
                    if (answerer == null) {
                        logger.error("invoke userService failed ! method selectByPrimaryKey , userId : [" + answererId + "]");
                    } else {
                        topAnswer.setAnswererName(answerer.getName());
                        topAnswer.setAnswererPicPath(answerer.getPicPath());
                        boolean existPraise = this.isExistPraise(answerId, currentUserId);
                        topAnswer.setIsPraise((byte)(existPraise ? 1 : 0));
                        Set<String> praiseUIdSet = this.getPraiseUIdSet(answerId);
                        if (CollectionUtils.isNotEmpty(praiseUIdSet)) {
                            topAnswer.setPraiseCount(praiseUIdSet.size());
                        }
                    }
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
            logger.error("invoke ask service failed! method: [ getCollectByUIdQuestionId ] id :" + id);
        }
        question.setIsCollect((byte)(collect == null ? 0 : 1));
        // set answerCount will go :ask_answer_answerCount_
        int answerCount = this.getAnswerCountByRedis(id);
        question.setAnswerCount(answerCount);
        logger.info("answerCount" + answerCount + "key: ask_answer_answerCount_" + id);
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
                logger.info("-----------convertAnswerUserList start ! answererId : " + answererId);
                User user = userService.selectByPrimaryKey(answererId);
                if (user == null) {
                    logger.error("convertAnswerUserList failed ! userId : " + answererId);
                    continue;
                }
                answer.setAnswererName(user.getName());
                answer.setAnswererPicPath(user.getPicPath());
                boolean existPraise = this.isExistPraise(id, currentUserId);
                logger.info("answerId = " + id + " userId = " + currentUserId + " existPraise ? " + existPraise);
                answer.setIsPraise((byte)(existPraise ? 1 : 0));
                Set<String> praiseUIdSet = this.getPraiseUIdSet(id);
                if (CollectionUtils.isNotEmpty(praiseUIdSet)) {
                    answer.setPraiseCount(praiseUIdSet.size());
                }
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
    private List<QuestionHome> convertMyAnswerList(List<QuestionHome> questionHomeList, User user, User otherUser) {

        if (CollectionUtils.isNotEmpty(questionHomeList)) {
            for (QuestionHome questionHome : questionHomeList) {
                if (questionHome == null)
                    continue;
                Question question = questionHome.getQuestion();
                if (question == null)
                    continue;
                long userId = question.getUserId();
                User qUser = null;
                try {
                    qUser = userService.getUserById(userId);
                } catch (Exception e) {
                    logger.error("invoke userService error. userId :{}", userId);
                }
                if (qUser == null)
                    continue;
                question.setUserName(qUser.getName());
                question.setPicPath(qUser.getPicPath());
                Answer answer = questionHome.getAnswer();
                if (answer == null)
                    continue;
                long id = answer.getId();
                answer.setAnswererName(user.getName());
                answer.setAnswererPicPath(user.getPicPath());
                if (otherUser != null) {
                    boolean existPraise = this.isExistPraise(id, otherUser.getId());
                    answer.setIsPraise((byte)(existPraise ? 1 : 0));
                } else {
                    boolean existPraise = this.isExistPraise(id, user.getId());
                    answer.setIsPraise((byte)(existPraise ? 1 : 0));
                }
                Set<String> praiseUIdSet = this.getPraiseUIdSet(id);
                if (CollectionUtils.isNotEmpty(praiseUIdSet)) {
                    answer.setPraiseCount(praiseUIdSet.size());
                }
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
                User user = null;
                try {
                    user = userService.getUserById(ownerId);
                } catch (Exception e) {
                    logger.error("invoke userService error. userId :{}", ownerId);
                }
                if (user == null)
                    continue;
                collect.setOwnerName(user.getName());
                collect.setOwnerPicPath(user.getPicPath());
            }
        }
        return collectList;
    }

    private void convertBodyQuestionToDBQuestion(Question dbQuestion, Question question) {

        dbQuestion.setAnswererType(question.getAnswererType());
        dbQuestion.setType(question.getType());
        dbQuestion.setTitle(question.getTitle());
        dbQuestion.setDescribe(question.getDescribe());
    }

    private void setReadCountByRedis(Question question) {

        long count = 0;
        long id = question.getId();
        long readCount = getReadCountByRedis(id);
        if (readCount > -1) {
            count = cache.incr(Constant.READ_COUNT_TAG + id);
            if (count < 1) {
                logger.error("invoke redis cache service failed! please check redis ....");
            } else {
                // 修改问题 浏览数
                question.setReadCount(count);
                try {
                    askService.updateQuestion(question);
                    logger.info("question:" + question.toString());
                } catch (Exception e) {
                    logger.error("invoke ask service failed! method : [ updateQuestion ]");
                }
            }
        }
    }

    private long getReadCountByRedis(long id) {

        long readCount = cache.getLongByRedis(Constant.READ_COUNT_TAG + id);
        if (readCount == 0) {
            try {
                readCount = askService.getReadCount(id);
            } catch (Exception e) {
                logger.error("invoke ask service failed! method : [ getReadCount ] id : " + id);
            }
            boolean flag = cache.setLongByRedis(Constant.READ_COUNT_TAG + id, readCount, 60 * 60 * 24);
            if (!flag) {
                logger.error("invoke redis cache service failed! please check redis ....");
            }
        }
        return readCount;
    }

    private MessageNotify createMessageNotify(String title, Answer answer, User user) {

        if (answer == null) {
            logger.error("answer is null! so skip createMessage!");
            return null;
        }
        MessageNotify message = new MessageNotify();
        message.setTitle("删除了问题" + title);
        message.setFromId(user.getId());
        message.setFromName(user.getName());
        message.setPicPath(user.getPicPath());
        //message.setType(MessageNotifyType.E);
        message.setType(16);
        message.setToId(answer.getAnswererId());
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

        Map<String, Object> map = new HashMap<String, Object>(3);
        //map.put("id", questionId);
        map.put("operation", 0);  // 0：删除通知
        //map.put("type", MessageNotifyType.EKnowledge.value());
        map.put("type", 16);
        return map;
    }

    private InterfaceResult convertMyQuestionPage(List<Question> questionList, long count, int start, int size) {

        Page<Question> page = new Page<Question>();
        page.setList(questionList);
        page.setTotalCount(count);
        page.setPageNo(start + 1);
        page.setPageSize(size);
        return InterfaceResult.getSuccessInterfaceResultInstance(page);
    }

    private InterfaceResult convertMyAnswerPage(List<QuestionHome> questionHomeList, long count, int start, int size) {

        Page<QuestionHome> page = new Page<QuestionHome>();
        page.setTotalCount(count);
        page.setList(questionHomeList);
        page.setPageNo(start + 1);
        page.setPageSize(size);
        return InterfaceResult.getSuccessInterfaceResultInstance(page);
    }

    private InterfaceResult convertMyCollectQuestionPage(List<QuestionCollect> questionCollectList, long count, int start, int size) {

        Page<QuestionCollect> page = new Page<QuestionCollect>();
        page.setTotalCount(count);
        page.setList(questionCollectList);
        page.setPageNo(start + 1);
        page.setPageSize(size);
        return InterfaceResult.getSuccessInterfaceResultInstance(page);
    }
}
