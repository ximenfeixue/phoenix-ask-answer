package com.ginkgocap.ywxt.interlocution.web.Task;

import com.ginkgocap.ywxt.interlocution.service.AskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;

/**
 * Created by Wang fei on 2017/6/15.
 */
public class DataSyncRedis implements InitializingBean, Runnable{

    private final Logger logger = LoggerFactory.getLogger(DataSyncRedis.class);

    @Resource
    private AskService askService;

    @Override
    public void afterPropertiesSet() throws Exception {

        logger.info("data sync start ...");
        new Thread().start();
        logger.info("data sync end ...");
    }

    @Override
    public void run() {

        updateData();
    }
    private void updateData() {

        int start = 0;
        int size = 20;

    }
}
