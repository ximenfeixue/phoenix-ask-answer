package com.ginkgocap.ywxt.interlocution.web.service;

import com.ginkgocap.parasol.associate.model.Associate;
import com.ginkgocap.ywxt.interlocution.model.Answer;
import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.model.QuestionBase;
import com.ginkgocap.ywxt.interlocution.model.QuestionHome;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/27.
 */
@Service("askServiceLocal")
public class AskServiceLocal {

    private final Logger logger = LoggerFactory.getLogger(AskServiceLocal.class);

    @Resource
    private AskService askService;

    @Resource
    private AssociateServiceLocal associateServiceLocal;

    @Resource
    private AnswerService answerService;

    public QuestionBase getQuestionById(long questionId, int start, int size) throws Exception{

        QuestionBase base = new QuestionBase();

        List<Answer> answerList = answerService.getAnswerListByQuestionId(questionId, start, size);

        Question question = askService.getQuestionById(questionId);

        //Question question = askService.getQuestionByIdAndUpdateReadCount(questionId);

        List<Associate> associateList = null;
        try {
            associateList = associateServiceLocal.getAssoByQuestionId(questionId);

        } catch (Exception e) {
            logger.error("invoke associate service failed!");
        }

        base.setQuestion(question);
        base.setAssociateList(associateList);
        base.setAnswerList(answerList);

        return base;
    }

    public List<QuestionHome> getAnswerByUId(long userId, int start, int size) throws Exception{

        List<QuestionHome> questionHomeList = new ArrayList<QuestionHome>();
        QuestionHome questionHome = new QuestionHome();
        QuestionHome base = null;
        List<Answer> answerList = null;
        try {
            answerList = answerService.getAnswerByUId(userId, start, size);
        } catch (Exception e) {
            logger.error("invoke answer service failed! method :[ getAnswerByUId ]");
        }
        if (CollectionUtils.isNotEmpty(answerList)) {
            for (Answer answer : answerList) {
                if (answer == null)
                    continue;
                long questionId = answer.getQuestionId();
                Question question = null;
                try {
                    question = askService.getQuestionById(questionId);
                } catch (Exception e) {
                    logger.error("invoke ask service failed! method :[ getQuestionById ]");
                }
                if (question != null) {
                    base = questionHome;
                    base.setAnswer(answer);
                    base.setQuestion(question);
                    questionHomeList.add(base);
                }
            }
        }
        return questionHomeList;
    }
}
