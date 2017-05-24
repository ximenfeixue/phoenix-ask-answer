package com.ginkgocap.ywxt.interlocution.service.impl;

import com.ginkgocap.ywxt.interlocution.id.DefaultIdGenerator;
import com.ginkgocap.ywxt.interlocution.id.IdGeneratorFactory;
import com.ginkgocap.ywxt.interlocution.service.AskAnswerCommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Wang fei on 2017/5/23.
 */
@Service("interlocutionCommonService")
public class AskAnswerCommonServiceImpl implements AskAnswerCommonService, InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(AskAnswerCommonServiceImpl.class);

    @Resource
    private MongoTemplate mongoTemplate;

    private static DefaultIdGenerator defaultIdGenerator = null;

    private AtomicInteger autoIncrease = new AtomicInteger(1);

    public Long getInterlocutionSequenceId() {
        {
            if (defaultIdGenerator != null) {
                try {
                    long sequenceId = Long.parseLong(defaultIdGenerator.next());
                    logger.info("generated  sequenceId： " + sequenceId);
                    return sequenceId;
                } catch (NumberFormatException ex) {
                    logger.error("生成唯一Id不是数字 ： " + ex.getMessage());
                    return tempId();
                } catch (Throwable ex) {
                    return tempId();
                }
            }

            return tempId();
        }
    }

    private synchronized Long tempId() {
        logger.error("唯一I的生成器出问题，请赶快排查");
        return System.currentTimeMillis() + autoIncrease.getAndIncrement();
    }
    public void afterPropertiesSet() throws Exception {
        defaultIdGenerator = IdGeneratorFactory.idGenerator(mongoTemplate);
        logger.info("Unique Id Generator init complete.");
    }
}
