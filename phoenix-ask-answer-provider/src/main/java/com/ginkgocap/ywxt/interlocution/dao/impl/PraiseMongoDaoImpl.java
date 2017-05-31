package com.ginkgocap.ywxt.interlocution.dao.impl;

import com.ginkgocap.ywxt.interlocution.dao.PraiseMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Constant;
import com.ginkgocap.ywxt.interlocution.model.Praise;
import com.ginkgocap.ywxt.interlocution.service.AskAnswerCommonService;
import com.gintong.frame.util.dto.InterfaceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * Created by Wang fei on 2017/5/31.
 */
@Repository("praiseMongoDao")
public class PraiseMongoDaoImpl implements PraiseMongoDao{

    private Logger logger = LoggerFactory.getLogger(PraiseMongoDaoImpl.class);

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private AskAnswerCommonService askAnswerCommonService;

    public Praise create(Praise praise) throws Exception {

        if (praise == null)
            throw new IllegalArgumentException("praise is null!");
        praise.setId(askAnswerCommonService.getInterlocutionSequenceId());
        praise.setAdmireTime(System.currentTimeMillis());
        mongoTemplate.save(praise, Constant.Collection.PRAISE);
        return praise;
    }

    public boolean delete(long answerId) throws Exception {

        Praise praise = null;
        Query query = new Query(Criteria.where(Constant.ANSWER_ID).is(answerId));
        try {
            praise = mongoTemplate.findAndRemove(query, Praise.class, Constant.Collection.PRAISE);

        } catch (Exception e) {
            logger.error("delete praise failed! [message]:" + e.getMessage());
        }
        return praise != null;
    }
}
