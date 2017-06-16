package com.ginkgocap.ywxt.interlocution.web.Task;

import com.ginkgocap.ywxt.cache.Cache;
import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Wang fei on 2017/6/15.
 */
public class DataSyncRedis implements InitializingBean, Runnable{

    private final Logger logger = LoggerFactory.getLogger(DataSyncRedis.class);

    @Resource
    private AskService askService;

    @Resource
    private AnswerService answerService;

    @Resource
    private Cache cache;

    @Override
    public void afterPropertiesSet() throws Exception {

        logger.info("data sync start ...");
        new Thread(this, "fei sync data .... ^-^").start();
        logger.info("data sync end ...");
    }

    @Override
    public void run() {

        try {
            updateData();
        } catch (Exception e) {
            logger.error("update data failed!");
        }
    }
    private void updateData() throws Exception{

        int total = 0;
        int start = 0;
        final int size = 20;
        List<Question> questionList = askService.getAllQuestion(start++, size);
        while (CollectionUtils.isNotEmpty(questionList)) {
            for (Question question : questionList) {
                long id = question.getId();
                int count = 0;
                try {
                    count = answerService.countAnswerByQuestionId(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                boolean flag = cache.setLongByRedis("ask_answer_answerCount_" + id, (long) count, 24 * 60 * 60);
                if (!flag) {
                    logger.error("invoke cache service method :[setLongByRedis] failed");
                }
                try {
                    askService.updateQuestionAnswerCount(id, count);
                    logger.info("update question answer count id = " + id + " count = " + count);
                } catch (Exception e) {
                    logger.error("invoke ask service method : [ updateQuestionAnswerCount ] failed! id : " + id);
                }
            }
            total += questionList.size();
            questionList = askService.getAllQuestion(start++, size);
        }
        logger.info("update question success size total = " + total);
    }
}
