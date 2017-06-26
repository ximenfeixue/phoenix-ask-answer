package com.ginkgocap.ywxt.interlocution.service.impl;


import com.ginkgocap.ywxt.interlocution.dao.AnswerMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Answer;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/27.
 */
@Service("answerService")
public class AnswerServiceImpl implements AnswerService {

    private final Logger logger = LoggerFactory.getLogger(AnswerServiceImpl.class);

    @Resource
    private AnswerMongoDao answerMongoDao;

    @Override
    public InterfaceResult insert(Answer answer) {

        Answer saveAnswer = null;
        Long id = null;
        try {
            saveAnswer = answerMongoDao.insert(answer);
            id = saveAnswer.getId();
        } catch (Exception e) {
            logger.error("insert answer failed!");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(id);
    }

    @Override
    public List<Answer> getAnswerListByQuestionId(long questionId, int start, int size) throws Exception{

        List<Answer> answerList = answerMongoDao.getAnswerListByQuestionId(questionId, start, size);

        return answerList;
    }

    @Override
    public Answer getAnswerByQuestionAndAnswererId(long questionId, long answererId) throws Exception {

        return answerMongoDao.getAnswerByQuestionAndAnswererId(questionId, answererId);
    }

    @Override
    public Answer getAnswerById(long id) throws Exception {

        return answerMongoDao.getAnswerById(id);
    }

    @Override
    public InterfaceResult updateAnswer(Answer answer) {

        try {
            answerMongoDao.updateAnswer(answer);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(true);
    }

    @Override
    public List<Answer> getAnswerByUId(long userId, int start, int size) throws Exception {

        return answerMongoDao.getAnswerByUId(userId, start, size);
    }

    @Override
    public InterfaceResult addTop(Answer answer) {

        InterfaceResult result = null;
        try {
            answer.setTop((byte) 1);
            answerMongoDao.updateAnswer(answer);
            result = InterfaceResult.getSuccessInterfaceResultInstance(answer);
        } catch (Exception e) {
            e.printStackTrace();
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return result;
    }

    @Override
    public InterfaceResult deleteTop(Answer answer) {

        InterfaceResult result = null;
        try {
            answer.setTop((byte) 0);
            answerMongoDao.updateAnswer(answer);
            result = InterfaceResult.getSuccessInterfaceResultInstance(answer);
        } catch (Exception e) {
            e.printStackTrace();
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return result;
    }

    @Override
    public Answer getAnswerMaxPraiseCountByQId(long questionId) throws Exception {

        return answerMongoDao.getAnswerMaxPraiseCountByQId(questionId);
    }

    @Override
    public InterfaceResult deleteAnswer(long id, long userId) {

        boolean flag;
        try {
            flag = answerMongoDao.deleteAnswer(id, userId);
        } catch (Exception e) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(flag);
    }

    @Override
    public long countAnswerByUId(long userId) throws Exception {

        return answerMongoDao.countAnswerByUId(userId);
    }

    @Override
    public List<Long> getAnswererIdListSet() throws Exception {

        return answerMongoDao.getAnswererIdListSet();
    }

    @Override
    public int countAnswerByQuestionId(long questionId) throws Exception {

        return answerMongoDao.countAnswerByQuestionId(questionId);
    }

    @Override
    public InterfaceResult deleteAnswerById(long id){

        boolean flag;
        try {
            flag = answerMongoDao.deleteAnswerById(id);
        } catch (Exception e) {
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        return InterfaceResult.getSuccessInterfaceResultInstance(flag);
    }

    @Override
    public int countTopAnswerByQuestionId(long questionId) throws Exception {

        return answerMongoDao.countTopAnswerByQuestionId(questionId);
    }

    @Override
    public List<Answer> searchAnswerByUser(List<Long> list, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception {

        return answerMongoDao.searchAnswerByUser(list, startTime, endTime, timeSortType, praiseCountSortType, start, size);
    }

    @Override
    public long countAnswerByUser(List<Long> list, long startTime, long endTime) {

        return answerMongoDao.countAnswerByUser(list, startTime, endTime);
    }

    @Override
    public List<Answer> searchAnswerByQuestionIdList(List<Long> list, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception {

        return answerMongoDao.searchAnswerByQuestionIdList(list, startTime, endTime, timeSortType, praiseCountSortType, start, size);
    }

    @Override
    public long countAnswerByQuestionIdList(List<Long> list, long startTime, long endTime) {

        return answerMongoDao.countAnswerByQuestionIdList(list, startTime, endTime);
    }

    @Override
    public List<Answer> searchAnswerByContent(String keyword, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception {

        return answerMongoDao.searchAnswerByContent(keyword, startTime, endTime, timeSortType, praiseCountSortType, start, size);
    }

    @Override
    public long countAnswerByContent(String keyword, long startTime, long endTime) {

        return answerMongoDao.countAnswerByContent(keyword, startTime, endTime);
    }

    @Override
    public boolean batchUpdateAnswerStatus(long questionId) {

        return answerMongoDao.batchUpdateAnswerStatus(questionId);
    }

    @Override
    public List<Answer> getAllAnswer(int start, int size) {

        return answerMongoDao.getAllAnswer(start, size);
    }

}
