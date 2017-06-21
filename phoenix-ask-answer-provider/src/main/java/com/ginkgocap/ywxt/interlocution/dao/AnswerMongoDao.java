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
     * 查询答案列表 通过 userId  $当问题不存在时 上级 会出现返回数据少$
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

    /**
     * 查询 问题的 答案数
     * @param questionId
     * @return
     * @throws Exception
     */
    int countAnswerByQuestionId(long questionId) throws Exception;

    /**
     * 删除答案 通过 id 运营后台 使用 。不需要验证答案属于者
     * @param id
     * @return
     * @throws Exception
     */
    boolean deleteAnswerById(long id) throws Exception;

    /**
     * 查询 置顶 答案数 通过 问题 id
     * @param questionId
     * @return
     * @throws Exception
     */
    int countTopAnswerByQuestionId(long questionId) throws Exception;

    /**
     * 搜索 答案 通过 用户 id list 后台运营
     * @param list
     * @param startTime
     * @param endTime
     * @param timeSortType
     * @param praiseCountSortType
     * @param start
     * @param size
     * @return
     * @throws Exception
     */
    List<Answer> searchAnswerByUser(List<Long> list, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception;

    /**
     * 搜索答案 通过 question id list
     * @param questionIdList
     * @param startTime
     * @param endTime
     * @param timeSortType
     * @param praiseCountSortType
     * @param start
     * @param size
     * @return
     * @throws Exception
     */
    List<Answer> searchAnswerByQuestionIdList(List<Long> questionIdList, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception;

    /**
     * 搜索答案 通过 答案 内容 模糊查询
     * @param keyword
     * @param startTime
     * @param endTime
     * @param timeSortType
     * @param praiseCountSortType
     * @param start
     * @param size
     * @return
     * @throws Exception
     */
    List<Answer> searchAnswerByContent(String keyword, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception;

    /**
     * 批量 修改 答案状态 通过 questionId
     * @param questionId
     * @return
     */
    boolean batchUpdateAnswerStatus(long questionId);

    /**
     * 查询 所有答案 分页
     * @param start
     * @param size
     * @return
     */
    List<Answer> getAllAnswer(int start, int size);
}
