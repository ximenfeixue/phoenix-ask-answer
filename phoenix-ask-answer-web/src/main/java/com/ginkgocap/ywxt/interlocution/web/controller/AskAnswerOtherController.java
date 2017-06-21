package com.ginkgocap.ywxt.interlocution.web.controller;

import com.ginkgocap.parasol.util.JsonUtils;
import com.ginkgocap.ywxt.interlocution.model.*;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import com.ginkgocap.ywxt.interlocution.web.service.AnswerServiceLocal;
import com.ginkgocap.ywxt.user.model.User;
import com.ginkgocap.ywxt.user.service.UserService;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Resource
    private UserService userService;

    private static final byte disabled = 1;

    private static final byte un_disabled = 0;

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
            Question question = null;
            try {
                question = askService.getQuestionById(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (question == null){
                result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
                result.getNotification().setNotifInfo("当前问题不存在或已删除");
                return result;
            }
            // check disabled state
            byte disabled = question.getDisabled();
            if (disabled > 0) {
                result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
                result.getNotification().setNotifInfo("当前问题已禁用，不允许置顶哦");
                return result;
            }
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

    /**
     * 禁用 问题
     * @param request
     * @return
     */
    @RequestMapping(value = "/disabled", method = RequestMethod.PUT)
    public InterfaceResult disabled(HttpServletRequest request) {

        InterfaceResult result = null;
        ID id = null;
        Question question = null;
        User yinUser = this.getYINUser(request);
        if (yinUser == null)
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        try {
            String requestJson = this.getBodyParam(request);
            id = (ID) JsonUtils.jsonToBean(requestJson, ID.class);
        } catch (Exception e) {
            logger.error("get body param error!");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        try {
            question = askService.getQuestionById(id.getId());
        } catch (Exception e) {
            logger.error("invoke ask service failed! method : [ getQuestionById ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        if (question == null) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            result.getNotification().setNotifInfo("当期问题不存在或已删除");
            return result;
        }
        long questionId = 0;
        try {
            questionId = question.getId();
            result = askService.updateDisabled(disabled, questionId);
        } catch (Exception e) {
            logger.error("invoke ask service failed ! method : [ updateDisabled ] id : " + questionId);
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        return result;
    }

    /**
     * 禁用 问题 取消
     * @param request
     * @return
     */
    @RequestMapping(value = "/disabled/{id}", method = RequestMethod.DELETE)
    public InterfaceResult disabled(HttpServletRequest request, @PathVariable long id) {

        InterfaceResult result = null;
        Question question = null;
        User yinUser = this.getYINUser(request);
        if (yinUser == null)
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        try {
            question = askService.getQuestionById(id);
        } catch (Exception e) {
            logger.error("invoke ask service failed! method : [ getQuestionById ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        if (question == null) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            result.getNotification().setNotifInfo("当期问题不存在或已删除");
            return result;
        }
        try {
            result = askService.updateDisabled(un_disabled, question.getId());
        } catch (Exception e) {
            logger.error("invoke ask service failed ! method : [ updateDisabled ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        return result;
    }

    /**
     * 搜索 问题 后台运营
     * 置顶 的问题 排序在前面
     * @param request
     * @param keyType 搜索类型 0：问题标题 1：发布人
     * @param keyword 模糊查询的 关键词
     * @param startTime 前端不查询 开始时间 则 传0
     * @param endTime 前端不查询 结束时间 则 传0
     * @param status 问题 状态 0：正常 1：禁用 -1：全部
     * @Param timeSortType 按照 发布时间排序 0：降序 1：升序
     * @Param readCountSortType 按照 阅读数排序 0：降序 1：升序
     * @Param answerCountSortType  按照 回答数排序 0：降序 1：升序
     * @param start 第 0,1,2...页
     * @param size 每页 显示条数
     * @return
     */
    @RequestMapping(value = "/search/question/{keyType}/{keyword}/{startTime}/{endTime}/{status}/{timeSortType}" +
            "/{readCountSortType}/{answerCountSortType}/{start}/{size}", method = RequestMethod.GET)
    public InterfaceResult searchQuestion(HttpServletRequest request, @PathVariable byte keyType,
                                          @PathVariable String keyword, @PathVariable long startTime,
                                          @PathVariable long endTime, @PathVariable byte status,
                                          @PathVariable byte timeSortType, @PathVariable byte readCountSortType,
                                          @PathVariable byte answerCountSortType, @PathVariable int start,
                                          @PathVariable int size) {

        List<Long> list = null;
        List<Question> questionList = null;
        User yinUser = this.getYINUser(request);
        if (yinUser == null)
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        if (keyType == 0) {
            try {
                if (StringUtils.isNotBlank(keyword)) {
                    questionList = askService.searchQuestionByTitle(keyword, startTime, endTime, status, timeSortType, readCountSortType, answerCountSortType, start, size);
                    questionList = convertQuestion(questionList);
                }
            } catch (Exception e) {
                logger.error("invoke ask service failed! method : [ searchQuestion ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        } else {
            try {
                if (StringUtils.isNotBlank(keyword)) {
                    list = returnListId(list, keyword);
                }
                if (CollectionUtils.isNotEmpty(list)) {
                    questionList = askService.searchQuestionByUser(list, startTime, endTime, status, timeSortType, readCountSortType, answerCountSortType, start, size);
                    questionList = convertQuestion(questionList);
                }
            } catch (Exception e) {
                logger.error("invoke ask service failed! method : [ searchQuestionByUser ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(questionList);
    }

    /**
     * 搜索 答案 后台运营
     * 置顶的 答案 排序在前面
     * @param request
     * @param keyType 搜索类型 0：问题标题 1：回答描述 2：回答人
     * @param keyword 模糊查询的 关键词
     * @param startTime
     * @param endTime
     * @Param timeSortType 按照 发布时间 排序 0：降序 1：升序
     * @Param praiseCountType 按照 点赞数 排序 0：降序 1：升序
     * @param start 第 0，1，2...页
     * @param size 每页 显示条数
     * @return
     */
    @RequestMapping(value = "/search/answer/{keyType}/{keyword}/{startTime}/{endTime}/{timeSortType}/{praiseCountSortType}/{start}/{size}", method = RequestMethod.GET)
    public InterfaceResult searchAnswer(HttpServletRequest request, @PathVariable byte keyType,
                                        @PathVariable String keyword, @PathVariable long startTime,
                                        @PathVariable long endTime, @PathVariable byte timeSortType,
                                        @PathVariable byte praiseCountSortType, @PathVariable int start,
                                        @PathVariable int size) {

        List<Answer> answerList = null;
        List<Long> list = null;
        User yinUser = this.getYINUser(request);
        if (yinUser == null)
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PERMISSION_EXCEPTION);
        if (endTime > startTime) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
        }
        if (keyType == 0) {
            try {
                if (StringUtils.isNotBlank(keyword)) {
                    answerList = answerServiceLocal.searchAnswerByQuestionTitle(keyword, startTime, endTime, timeSortType, praiseCountSortType, start, size);
                    answerList = converAnswer(answerList);
                }
            } catch (Exception e) {
                logger.error("invoke answer local service failed! method : [ searchAnswerByQuestionTitle ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        } else if (keyType == 1) {
            try {
                if (StringUtils.isNotBlank(keyword)) {
                    answerList = answerService.searchAnswerByContent(keyword, startTime, endTime, timeSortType, praiseCountSortType, start, size);
                    answerList = converAnswer(answerList);
                }
            } catch (Exception e) {
                logger.error("invoke answer service failed! method : [ searchAnswerByContent ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        } else if (keyType == 2) {
            try {
                if (StringUtils.isNotBlank(keyword)) {
                    list = returnListId(list, keyword);
                }
                if (CollectionUtils.isNotEmpty(list)) {
                    answerList = answerService.searchAnswerByUser(list, startTime, endTime, timeSortType, praiseCountSortType, start, size);
                    answerList = converAnswer(answerList);
                }
            } catch (Exception e) {
                logger.error("invoke answer service failed! method : [ searchAnswerByUser ]");
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
            }
        } else {}

        return InterfaceResult.getSuccessInterfaceResultInstance(answerList);
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

    /**
     * 通过 name 关键字 查询 userId list
     * @param list
     * @param keyword
     * @return
     */
    private List<Long> returnListId(List<Long> list, String keyword) {

        Map<String, Object> map = null;
        try {
            map = userService.selectByMember(keyword, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (map != null) {
            List<User> userList = (List<User>) map.get("rows");
            if (CollectionUtils.isNotEmpty(userList)) {
                list = new ArrayList<Long>(userList.size());
                for (User user : userList) {
                    if (user == null)
                        continue;
                    long userId = user.getId();
                    list.add(userId);
                }
            }
        }
        return list;
    }

    private List<Question> convertQuestion(List<Question> questionList) {

        if (CollectionUtils.isNotEmpty(questionList)) {
            for (Question question : questionList) {
                if (question == null)
                    continue;
                long userId = question.getUserId();
                User user = userService.selectByPrimaryKey(userId);
                question.setUserName(user.getName());
                question.setPicPath(user.getPicPath());
                question.setVirtual(user.isVirtual() ? (short) 1 : (short) 0);
            }
        }
        return questionList;
    }

    private List<Answer> converAnswer(List<Answer> answerList) {

        if (CollectionUtils.isNotEmpty(answerList)) {
            for (Answer answer : answerList) {
                if (answer == null)
                    continue;
                long answererId = answer.getAnswererId();
                User user = userService.selectByPrimaryKey(answererId);
                answer.setAnswererName(user.getName());
                answer.setAnswererPicPath(user.getPicPath());
                answer.setVirtual(user.isVirtual() ? (short) 1 : (short) 0);
            }
        }
        return answerList;
    }
}
