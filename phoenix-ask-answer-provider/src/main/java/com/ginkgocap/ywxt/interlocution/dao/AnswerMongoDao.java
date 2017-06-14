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

    /**
     * 查询答案列表 通过 userId
     * @param userId
     * @return
     * @throws Exception
     */
    List<Answer> getAnswerByUId(long userId, int start, int size) throws Exception;

    /**
     * 查询最多点赞数的 答案 若有多个 取第一条
     * @param questionId
     * @return
     * @throws Exception
     */
    Answer getAnswerMaxPraiseCountByQId(long questionId) throws Exception;

    /**
     * 删除 答案 只能删除自己的
     * @param id
     * @param userId
     * @return
     * @throws Exception
     */
    boolean deleteAnswer(long id, long userId) throws Exception;

    /**
     * 查询 用户 回答数量
     * @param userId
     * @return
     * @throws Exception
     */
    long countAnswerByUId(long userId) throws Exception;

    /**
     * 查询 表中 所有回答者 id
     * @return
     * @throws Exception
     */
    List<Long> getAnswererIdListSet() throws Exception;
}
