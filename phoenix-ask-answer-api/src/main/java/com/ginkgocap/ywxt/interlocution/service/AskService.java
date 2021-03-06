package com.ginkgocap.ywxt.interlocution.service;

import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.model.QuestionCollect;
import com.ginkgocap.ywxt.interlocution.model.QuestionReport;
import com.gintong.frame.util.dto.InterfaceResult;

import java.util.List;

/**
 * Created by Wang fei on 2017/5/23.
 */
public interface AskService {

    /**
     * 插入数据
     * @param question
     * @return
     */
    InterfaceResult insert(Question question);
    /**
     * 获取 发现 问答
     * @param status
     * @param start
     * @param size
     * @return
     */
    List<Question> getAllAskAnswerByStatus(byte status, int start, int size) throws Exception;
    /**
     * 通过 主键id 查询 问题
     * @param id
     * @return
     * @throws Exception
     */
    Question getQuestionById(long id) throws Exception;
    /**
     * 通过 主键 id 查询问题 并增加 阅读数
     * @param id
     * @return
     * @throws Exception
     */
    Question getQuestionByIdAndUpdateReadCount(long id) throws Exception;
    /**
     * 修改 问题 是否被答的状态
     * @param id
     * @return
     * @throws Exception
     */
    boolean updateStatus(long id) throws Exception;
    /**
     * 修改 问题 是否被回答 和 答案数
     * @param id
     * @param status
     * @return
     * @throws Exception
     */
    boolean updateStatusAndAnswerCount(long id, byte status, int answerCount) throws Exception;
    /**
     * 修改 问题
     * @param question
     * @return
     */
    InterfaceResult updateQuestion(Question question);
    /**
     * 查询问题 通过 userId
     * @param userId
     * @param start
     * @param size
     * @return
     * @throws Exception
     */
    List<Question> getQuestionByUId(long userId, int start, int size) throws Exception;

    /**
     * 查询 个数 通过 userId
     * @param userId
     * @return
     */
    long countQuestionByUId(long userId);
    /**
     * 收藏 问题
     * @param collect
     * @return
     */
    InterfaceResult addCollect(QuestionCollect collect);
    /**
     * 取消 收藏 问题
     * @param questionId
     * @param userId
     * @return
     */
    InterfaceResult deleteCollect(long questionId, long userId);
    /**
     * 举报 问题
     * @param report
     * @return
     */
    InterfaceResult addReport(QuestionReport report);
    /**
     * 查询 我的 收藏问题列表
     * @param userId
     * @param start
     * @param size
     * @return
     * @throws Exception
     */
    List<QuestionCollect> getCollectByUId(long userId, int start, int size) throws Exception;

    /**
     * 查询 我的 收藏 列表 个数
     * @param userId
     * @return
     */
    long countQuestionCollectByUId(long userId);
    /**
     * 查询收藏问题 通过 userId ，questionId
     * @param userId
     * @param questionId
     * @return
     * @throws Exception
     */
    QuestionCollect getCollectByUIdQuestionId(long userId, long questionId) throws Exception;
    /**
     * 删除问题 通过 id 运营后台 不需要验证问题属于者
     * @param id
     * @return
     */
    InterfaceResult deleteQuestion(long id);
    /**
     * 删除 问题 只能 删除自己的
     * @param id
     * @return
     */
    InterfaceResult deleteQuestion(long id, long userId);
    /**
     * 查看 阅读数
     * @param id
     * @return
     * @throws Exception
     */
    Long getReadCount(long id) throws Exception;
    /**
     * 置顶 问题
     * @param id
     * @return
     * @throws Exception
     */
    InterfaceResult addTop(long id);
    /**
     * 取消 置顶 问题
     * @param id
     * @return
     * @throws Exception
     */
    InterfaceResult deleteTop(long id);
    /**
     * 获取 所有 问题
     * @param start
     * @param size
     * @return
     * @throws Exception
     */
    List<Question> getAllQuestion(int start, int size) throws Exception;
    /**
     * 修改 问题 回答数
     * @param id
     * @param count
     * @return
     */
    InterfaceResult updateQuestionAnswerCount(long id, int count);
    /**
     * 修改 问题 禁用 状态
     * @param disabled
     * @param questionId
     * @return
     */
    InterfaceResult updateDisabled(byte disabled, long questionId);
    /**
     * 通过 id list 查询 问题
     * @param userIdList 创建问题者 list
     * @param startTime
     * @param endTime
     * @param status 问题 状态
     * @param timeSortType 时间 排序
     * @param readCountSortType 阅读数 排序
     * @param answerCountSortType 答案数 排序
     * @param start
     * @param size
     * @return
     */
    List<Question> searchQuestionByUser(List<Long> userIdList, long startTime, long endTime, byte status, byte timeSortType, byte readCountSortType, byte answerCountSortType, int start, int size);

    /**
     * 通过 id list 查询 问题 总个数
     * @param userIdList 创建问题者 list
     * @param startTime
     * @param endTime
     * @param status 问题 状态
     * @return
     */
    long countQuestionByUser(List<Long> userIdList, long startTime, long endTime, byte status);
    /**
     * 通过问题标题 模糊查询 问题
     * @param keyword 标题关键字
     * @param startTime
     * @param endTime
     * @param status 问题 状态
     * @param timeSortType 时间 排序
     * @param readCountSortType 阅读数 排序
     * @param answerCountSortType 答案数 排序
     * @param start
     * @param size
     * @return
     */
    List<Question> searchQuestionByTitle(String keyword, long startTime, long endTime, byte status, byte timeSortType, byte readCountSortType, byte answerCountSortType, int start, int size);

    /**
     * 通过问题标题 模糊查询 问题 总个数
     * @param keyword 标题关键字
     * @param startTime
     * @param endTime
     * @param status 问题 状态
     * @return
     */
    long countQuestionByTitle(String keyword, long startTime, long endTime, byte status);
}
