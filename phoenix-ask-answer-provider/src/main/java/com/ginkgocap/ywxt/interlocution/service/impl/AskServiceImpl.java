package com.ginkgocap.ywxt.interlocution.service.impl;

import com.ginkgocap.ywxt.interlocution.dao.AskMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.model.QuestionCollect;
import com.ginkgocap.ywxt.interlocution.model.QuestionReport;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public QuestionCollect getCollectByUIdQuestionId(long userId, long questionId) throws Exception {

        return askMongoDao.getCollectByUIdQuestionId(userId, questionId);
    }
}
