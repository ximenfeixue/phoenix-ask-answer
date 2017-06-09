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
     * 查询收藏问题 通过 userId ，questionId
     * @param userId
     * @param questionId
     * @return
     * @throws Exception
     */
    QuestionCollect getCollectByUIdQuestionId(long userId, long questionId) throws Exception;
    /**
     * 删除问题 通过 id
     * @param id
     * @return
     */
    InterfaceResult deleteQuestion(long id);
}
