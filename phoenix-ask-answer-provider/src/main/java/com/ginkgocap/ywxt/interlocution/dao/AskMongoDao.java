package com.ginkgocap.ywxt.interlocution.dao;


import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.model.QuestionCollect;
import com.ginkgocap.ywxt.interlocution.model.QuestionReport;

import java.util.List;

/**
 * Created by Wang fei on 2017/5/24.
 */
public interface AskMongoDao {

    Question insert(Question question) throws Exception;

    List<Question> getAllAskAnswerByStatus(byte status, int start, int size) throws Exception;

    Question getQuestionById(long id) throws Exception;

    boolean updateStatus(long id) throws Exception;

    void update(Question question) throws Exception;

    List<Question> getQuestionByUId(long userId, int start, int size) throws Exception;

    QuestionCollect addCollect(QuestionCollect collect) throws Exception;

    boolean deleteCollect(long questionId, long userId) throws Exception;

    QuestionReport addReport(QuestionReport report) throws Exception;

    List<QuestionCollect> getCollectByUId(long userId, int start, int size) throws Exception;

    QuestionCollect getCollectByUIdQuestionId(long userId, long questionId) throws Exception;
}
