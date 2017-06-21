package com.ginkgocap.ywxt.interlocution.dao.impl;

import com.ginkgocap.ywxt.interlocution.dao.DataSyncMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Constant;
import com.ginkgocap.ywxt.interlocution.model.DataSync;
import com.ginkgocap.ywxt.interlocution.service.AskAnswerCommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/31.
 */
@Repository("dataSyncMongoDao")
public class DataSyncMongoDaoImpl implements DataSyncMongoDao{

    private final Logger logger = LoggerFactory.getLogger(DataSyncMongoDaoImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Resource
    private AskAnswerCommonService askAnswerCommonService;

    private final int maxSize = 50;

    public long saveDataSync(DataSync data) {

        Long sequenceId = askAnswerCommonService.getInterlocutionSequenceId();
        data.setId(sequenceId);
        try {
            mongoTemplate.insert(data, Constant.Collection.DATA_SYNC);

        } catch (Exception e) {
            logger.error("save dataSync failed! id :" + sequenceId + "[message]" + e.getMessage());
            return -1;
        }
        return sequenceId;
    }

    public List<DataSync> batchSaveDataSync(List<DataSync> dataList) {

        for (DataSync data : dataList) {
            data.setId(askAnswerCommonService.getInterlocutionSequenceId());
        }
        try {
            mongoTemplate.insert(dataList, Constant.Collection.DATA_SYNC);
        } catch (Exception e) {
            logger.error("save dataSync failed! [message]" + e.getMessage());
            return null;
        }
        return dataList;
    }

    public boolean deleteDataSync(final long id) {

        DataSync dataSync = null;
        try {
            Query query = new Query(Criteria.where(Constant._ID).is(id));
            dataSync = mongoTemplate.findAndRemove(query, DataSync.class, Constant.Collection.DATA_SYNC);

        } catch (Exception e) {
            logger.error("delete dataSync failed! id:" + id + "[message]:" + e.getMessage());
            return false;
        }
        return dataSync != null;
    }

    public List<DataSync> getDataSyncList() {

        List<DataSync> dataSyncList = null;
        try {
            Query query = new Query();
            query.skip(0);
            query.limit(maxSize);
            dataSyncList = mongoTemplate.find(query, DataSync.class, Constant.Collection.DATA_SYNC);

        } catch (Exception e) {
            logger.error("query dataSync failed ! please mongo db service!");
        }
        return dataSyncList;
    }
}
