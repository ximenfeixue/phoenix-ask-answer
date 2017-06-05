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
        result = answerService.addTop(answer);
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
}
