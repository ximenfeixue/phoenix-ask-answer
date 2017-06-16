package com.ginkgocap.ywxt.interlocution.web.service;

import com.ginkgocap.ywxt.interlocution.model.Answer;
import com.ginkgocap.ywxt.interlocution.model.PartAnswer;
import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import com.ginkgocap.ywxt.interlocution.web.controller.BaseController;
import com.gintong.frame.util.dto.CommonResultCode;
import com.gintong.frame.util.dto.InterfaceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by Wang fei on 2017/6/5.
 */
@Service("answerServiceLocal")
public class AnswerServiceLocal extends BaseController{

    private final Logger logger = LoggerFactory.getLogger(AskServiceLocal.class);

    @Resource
    private AnswerService answerService;

    @Resource
    private AskService askService;

    public InterfaceResult addTop(long id) {

        InterfaceResult result = null;
        Answer answer = null;
        PartAnswer partAnswer = new PartAnswer();
        try {
            answer = answerService.getAnswerById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        if (answer == null) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            result.getNotification().setNotifInfo("当前答案不存在或已删除");
            return result;
        }
        // 将答案 置顶
        try {
            result = answerService.addTop(answer);
        } catch (Exception e) {
            logger.error("invoke answer service failed！ please check service");
        }
        if ("0".equals(result.getNotification().getNotifCode())) {
            long questionId = answer.getQuestionId();
            Question question = null;
            try {
                question = askService.getQuestionById(questionId);
            } catch (Exception e) {
                e.printStackTrace();
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
            }
            if (question == null) {
                result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
                result.getNotification().setNotifInfo("当前问题不存在或已删除");
                return result;
            }
            PartAnswer topAnswer = question.getTopAnswer();
            topAnswer = partAnswer;
            Answer resultAnswer = (Answer)result.getResponseData();
            topAnswer = convertAnswer(resultAnswer);
            question.setTopAnswer(topAnswer);
            try {
                askService.updateQuestion(question);
            } catch (Exception e) {
                logger.error("invoke ask service failed! method :[ updateQuestion ]");
            }
        }
        return result;
    }

    public InterfaceResult deleteTop(long id) {

        InterfaceResult result = null;
        Answer answer = null;
        PartAnswer partAnswer = new PartAnswer();
        try {
            answer = answerService.getAnswerById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
        }
        if (answer == null) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            result.getNotification().setNotifInfo("当前答案不存在或已删除");
            return result;
        }
        // 取消 置顶
        result = answerService.deleteTop(answer);
        if ("0".equals(result.getNotification().getNotifCode())) {
            Answer resultAnswer = (Answer) result.getResponseData();
            Question question = null;
            try {
                question = askService.getQuestionById(resultAnswer.getQuestionId());

            } catch (Exception e) {
                e.printStackTrace();
                return InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_DB_OPERATION_EXCEPTION);
            }
            if (question == null) {
                result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
                result.getNotification().setNotifInfo("当前问题不存在或已删除");
                return result;
            }
            PartAnswer topAnswer = question.getTopAnswer();
            topAnswer = partAnswer;
            // 查询 点赞最多的 答案 放到最优的答案中
            Answer maxPraiseCountAnswer = null;
            try {
                maxPraiseCountAnswer = answerService.getAnswerMaxPraiseCountByQId(answer.getQuestionId());
            } catch (Exception e) {
                logger.error("invoke answer service failed! method :[ getAnswerMaxPraiseCountByQId ]");
            }
            topAnswer = convertAnswer(maxPraiseCountAnswer);
            question.setTopAnswer(topAnswer);
            try {
                askService.updateQuestion(question);
            } catch (Exception e) {
                logger.error("invoke ask service failed! method :[ updateQuestion ]");
            }
        }
        return result;
    }

    public InterfaceResult removeAnswer(long id) {

        InterfaceResult result = null;
        Answer answer = null;
        Question question = null;
        try {
            answer = answerService.getAnswerById(id);
        } catch (Exception e) {
            logger.error("invoke answer service failed! method :[ getAnswerById ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        if (answer == null) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            result.getNotification().setNotifInfo("当前答案不存在或已删除");
            return result;
        }
        long questionId = answer.getQuestionId();
        try {
            question = askService.getQuestionById(questionId);
        } catch (Exception e) {
            logger.error("invoke ask service failed! method : [ getQuestionById ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        if (question == null) {
            result = InterfaceResult.getInterfaceResultInstance(CommonResultCode.PARAMS_EXCEPTION);
            result.getNotification().setNotifInfo("当前问题不存在或已删除");
            return result;
        }
        // 删除答案
        try {
            result = answerService.deleteAnswerById(id);
        } catch (Exception e) {
            logger.error("invoke answer service failed! method : [ deleteAnswer ]");
            return InterfaceResult.getInterfaceResultInstance(CommonResultCode.SYSTEM_EXCEPTION);
        }
        // 删除 答案 成功 后 所做操作
        afterRemoveAnswer(result, question, id);
        return result;
    }

    /**
     * 删除 答案后 所做操作
     * @param result
     * @param question
     * @param id 删除的 答案 id
     */
    public void afterRemoveAnswer(InterfaceResult result, Question question, long id) {

        if ("0".equals(result.getNotification().getNotifCode())) {
            logger.info("method [ afterRemoveAnswer ] start ......");
            long questionId = question.getId();
            PartAnswer topAnswer = question.getTopAnswer();
            if (topAnswer != null && topAnswer.getAnswerId() == id) {
                Answer maxPraiseCountAnswer = null;
                try {
                    maxPraiseCountAnswer = answerService.getAnswerMaxPraiseCountByQId(questionId);
                } catch (Exception e) {
                    logger.error("invoke answer service failed! method : [ getAnswerMaxPraiseCountByQId ] questionId :" + questionId);
                }
                PartAnswer partAnswer = null;
                if (maxPraiseCountAnswer != null && maxPraiseCountAnswer.getPraiseCount() > 0) {
                    partAnswer = convertAnswer(maxPraiseCountAnswer);
                }
                question.setTopAnswer(partAnswer);
            }
            // 修改 问题表 中 status and answerCount 字段
            int answerCount = (int)minusAnswerCountByRedis(questionId);
            logger.info("answerCount:" + answerCount);
            byte status = 0;
            if (answerCount > 0) {
                status = 1;
            } else {
                answerCount = 0;
            }
            question.setStatus(status);
            question.setAnswerCount(answerCount);
            try {
                askService.updateQuestion(question);

            } catch (Exception e) {
                logger.error("invoke ask service failed! method : [ updateQuestion ]");
            }
            logger.info("method [ afterRemoveAnswer ] end ......");
        }
    }
}
