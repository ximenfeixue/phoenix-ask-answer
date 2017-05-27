package com.ginkgocap.ywxt.interlocution.service.impl;

import com.ginkgocap.ywxt.interlocution.dao.AskMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Question;
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
}
