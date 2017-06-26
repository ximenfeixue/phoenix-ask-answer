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

    int countTopAnswerByQuestionId(long questionId) throws Exception;

    List<Answer> searchAnswerByUser(List<Long> list, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception;

    long countAnswerByUser(List<Long> list, long startTime, long endTime);

    List<Answer> searchAnswerByQuestionIdList(List<Long> list, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception;

    long countAnswerByQuestionIdList(List<Long> list, long startTime, long endTime);

    List<Answer> searchAnswerByContent(String keyword, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception;

    long countAnswerByContent(String keyword, long startTime, long endTime);

    boolean batchUpdateAnswerStatus(long questionId);

    List<Answer> getAllAnswer(int start, int size);
}
