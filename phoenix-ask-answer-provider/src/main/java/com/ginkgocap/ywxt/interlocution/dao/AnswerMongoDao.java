package com.ginkgocap.ywxt.interlocution.dao;

import com.ginkgocap.ywxt.interlocution.model.Answer;

import java.util.List;

/**
 * Created by Wang fei on 2017/5/27.
 */
public interface AnswerMongoDao {

    /**
     * 通过问题 id 查询 所有答案 分页
     * @param questionId
     * @param start
     * @param size
     * @return
     */
    List<Answer> getAnswerListByQuestionId(long questionId, int start, int size);

    /**
     * 插入 答案 数据
     * @param answer
     * @return
     */
    Answer insert(Answer answer) throws Exception;

    /**
     * 查询 答案 通过 问题 id 和 回答者 id
     */
    Answer getAnswerByQuestionAndAnswererId(long questionId, long answererId) throws Exception;

    /**
     * 查询答案 通过 id
     * @param id
     * @return
     * @throws Exception
     */
    Answer getAnswerById(long id) throws Exception;

    /**
     * 修改答案
     * @param answer
     * @return
     * @throws Exception
     */
    boolean updateAnswer(Answer answer) throws Exception;
}
