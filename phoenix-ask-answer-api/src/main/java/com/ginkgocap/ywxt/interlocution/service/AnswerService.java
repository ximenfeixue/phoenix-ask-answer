package com.ginkgocap.ywxt.interlocution.service;

import com.ginkgocap.ywxt.interlocution.model.Answer;
import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.user.model.User;
import com.gintong.frame.util.dto.InterfaceResult;

import java.util.List;

/**
 * Created by Wang fei on 2017/5/27.
 */
public interface AnswerService {

    InterfaceResult insert(Answer answer);

    List<Answer> getAnswerListByQuestionId(long questionId, int start, int size) throws Exception;

    Answer getAnswerByQuestionAndAnswererId(long questionId, long answererId) throws Exception;

    Answer getAnswerById(long id) throws Exception;

    InterfaceResult updateAnswer(Answer answer);

    List<Answer> getAnswerByUId(long userId, int start, int size) throws Exception;

    InterfaceResult addTop(Answer answer);

    InterfaceResult deleteTop(Answer answer);

    Answer getAnswerMaxPraiseCountByQId(long questionId) throws Exception;

    InterfaceResult deleteAnswer(long id, long userId);

    long countAnswerByUId(long userId) throws Exception;

    List<Long> getAnswererIdListSet() throws Exception;

    int countAnswerByQuestionId(long questionId) throws Exception;

    InterfaceResult deleteAnswerById(long id);
}
