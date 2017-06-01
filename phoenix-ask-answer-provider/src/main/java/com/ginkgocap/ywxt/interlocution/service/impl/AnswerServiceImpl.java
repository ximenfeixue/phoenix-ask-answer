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
}
