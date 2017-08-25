package com.ginkgocap.ywxt.interlocution.service.impl;

import com.ginkgocap.ywxt.interlocution.dao.AskMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.model.QuestionCollect;
import com.ginkgocap.ywxt.interlocution.model.QuestionReport;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/23.
 */
@Service("askService")
public class AskServiceImpl implements AskService {

    private final Logger logger = LoggerFactory.getLogger(AskServiceImpl.class);

    @Autowired
    private AskMongoDao askMongoDao;

    @Override
    public InterfaceResult insert(Question question) {

        Question saveQuestion = null;
        Long id = null;
        try {
            saveQuestion = askMongoDao.insert(question);
            id = saveQuestion.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SUCCESS, id);
    }

    @Override
    public List<Question> getAllAskAnswerByStatus(byte status, int start, int size) throws Exception {

        List<Question> questionList = askMongoDao.getAllAskAnswerByStatus(status, start, size);

        return questionList;
    }

    @Override
    public Question getQuestionById(long id) throws Exception {

        Question question = askMongoDao.getQuestionById(id);

        return question;
    }

    @Override
    public Question getQuestionByIdAndUpdateReadCount(long id) throws Exception {

        Question question = askMongoDao.getQuestionById(id);

        return null;
    }

    @Override
    public boolean updateStatus(long id) throws Exception {

        return askMongoDao.updateStatus(id);
    }

    @Override
    public boolean updateStatusAndAnswerCount(long id, byte status, int answerCount) throws Exception {

        return askMongoDao.updateStatusAndAnswerCount(id, status, answerCount);
    }

    @Override
    public InterfaceResult updateQuestion(Question question) {

        try {
            askMongoDao.update(question);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(true);
    }

    @Override
    public List<Question> getQuestionByUId(long userId, int start, int size) throws Exception {

        return askMongoDao.getQuestionByUId(userId, start, size);
    }

    @Override
    public long countQuestionByUId(long userId) {

        return askMongoDao.countQuestionByUId(userId);
    }

    @Override
    public InterfaceResult addCollect(QuestionCollect collect) {

        QuestionCollect saveCollect = null;
        long id = 0;
        try {
            saveCollect = askMongoDao.addCollect(collect);
            id = saveCollect.getId();
        } catch (Exception e) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(id);
    }

    @Override
    public InterfaceResult deleteCollect(long questionId, long userId) {

        try {
            askMongoDao.deleteCollect(questionId, userId);
        } catch (Exception e) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SUCCESS);
    }

    @Override
    public InterfaceResult addReport(QuestionReport report) {

        QuestionReport saveReport = null;
        long id = 0;
        try {
            saveReport = askMongoDao.addReport(report);
            id = saveReport.getId();
        } catch (Exception e) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(id);
    }

    @Override
    public List<QuestionCollect> getCollectByUId(long userId, int start, int size) throws Exception {

        return askMongoDao.getCollectByUId(userId, start, size);
    }

    @Override
    public long countQuestionCollectByUId(long userId) {

        return askMongoDao.countQuestionCollectByUId(userId);
    }

    @Override
    public QuestionCollect getCollectByUIdQuestionId(long userId, long questionId) throws Exception {

        return askMongoDao.getCollectByUIdQuestionId(userId, questionId);
    }

    @Override
    public InterfaceResult deleteQuestion(long id) {

        boolean flag;
        try {
            flag = askMongoDao.deleteQuestion(id);
        } catch (Exception e) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(flag);
    }

    @Override
    public InterfaceResult deleteQuestion(long id, long userId) {

        boolean flag;
        try {
            flag = askMongoDao.deleteQuestion(id, userId);
        } catch (Exception e) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(flag);
    }

    @Override
    public Long getReadCount(long id) throws Exception {

        Long readCount = null;
        try {
            readCount = askMongoDao.getReadCount(id);
        } catch (Exception e) {
            logger.error("mongo find failed! method : [ getReadCount ]  id :" + id);
        }
        return readCount;
    }

    @Override
    public InterfaceResult addTop(long id) {

        boolean flag;
        try {
            flag = askMongoDao.addTop(id);
        } catch (Exception e) {
            logger.error("mongo top failed! method : [ addTop ]  id :" + id);
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(flag);
    }

    @Override
    public InterfaceResult deleteTop(long id) {

        boolean flag;
        try {
            flag = askMongoDao.deleteTop(id);
        } catch (Exception e) {
            logger.error("mongo delete top failed! method : [ deleteTop ]  id :" + id);
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(flag);
    }

    @Override
    public List<Question> getAllQuestion(int start, int size) throws Exception {

        return askMongoDao.getAllQuestion(start, size);
    }

    @Override
    public InterfaceResult updateQuestionAnswerCount(long id, int count) {

        boolean flag;
        try {
            flag = askMongoDao.updateQuestionAnswerCount(id, count);
        } catch (Exception e) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(flag);
    }

    @Override
    public InterfaceResult updateDisabled(byte disabled, long questionId) {

        boolean flag;
        try {
            flag = askMongoDao.updateDisabled(disabled, questionId);
        } catch (Exception e) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(flag);
    }

    @Override
    public List<Question> searchQuestionByUser(List<Long> userIdList, long startTime, long endTime, byte status, byte timeSortType, byte readCountSortType, byte answerCountSortType, int start, int size) {

        return askMongoDao.searchQuestionByUser(userIdList, startTime, endTime, status, timeSortType, readCountSortType, answerCountSortType, start, size);
    }

    @Override
    public long countQuestionByUser(List<Long> userIdList, long startTime, long endTime, byte status) {

        return askMongoDao.countQuestionByUser(userIdList, startTime, endTime, status);
    }

    @Override
    public List<Question> searchQuestionByTitle(String keyword, long startTime, long endTime, byte status, byte timeSortType, byte readCountSortType, byte answerCountSortType, int start, int size) {

        return askMongoDao.searchQuestionByTitle(keyword, startTime, endTime, status, timeSortType, readCountSortType, answerCountSortType, start, size);
    }

    @Override
    public long countQuestionByTitle(String keyword, long startTime, long endTime, byte status) {

        return askMongoDao.countQuestionByTitle(keyword, startTime, endTime, status);
    }

    public List getCreateQuestionByUserId(long userId, String startTime, String endTime) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = sdf.parse(startTime);
        Date endDate = sdf.parse(endTime);
        long start = startDate.getTime();
        long end = endDate.getTime();
        return askMongoDao.getCreateQuestionByUserId(userId, start, end);
    }
}
