package com.ginkgocap.ywxt.interlocution.dao.impl;

import com.ginkgocap.ywxt.interlocution.dao.PraiseMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Constant;
import com.ginkgocap.ywxt.interlocution.model.Praise;
import com.ginkgocap.ywxt.interlocution.model.QuestionCollect;
import com.ginkgocap.ywxt.interlocution.service.AskAnswerCommonService;
import com.gintong.frame.util.dto.InterfaceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/31.
 */
@Repository("praiseMongoDao")
public class PraiseMongoDaoImpl implements PraiseMongoDao {

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

    public boolean delete(long answerId, long userId) throws Exception {

        Praise praise = null;
        Query query = new Query();
        query.addCriteria(Criteria.where(Constant.ANSWER_ID).is(answerId));
        query.addCriteria(Criteria.where("admirerId").is(userId));
        try {
            praise = mongoTemplate.findAndRemove(query, Praise.class, Constant.Collection.PRAISE);

        } catch (Exception e) {
            logger.error("delete praise failed! [message]:" + e.getMessage());
        }
        return praise != null;
    }

    public List<Praise> getPraiseUser(long answerId, int start, int size) throws Exception {

        if (answerId < 0 || start < 0 || size < 0)
            throw new IllegalArgumentException("answerId < 0");
        Query query = new Query(Criteria.where("answerId").is(answerId));
        long count = mongoTemplate.count(query, QuestionCollect.class, Constant.Collection.PRAISE);
        int index = start * size;
        if (index > count) {
            logger.error("because of index > count , so return null .index :" + index + "count :" + count);
            return null;
        }
        query.with(new Sort(Sort.Direction.DESC, Constant.CREATE_TIME));
        query.skip(index);
        query.limit(size);
        return mongoTemplate.find(query, Praise.class, Constant.Collection.PRAISE);
    }

    public long countByAnswerId(long answerId) throws Exception {

        Query query = new Query(Criteria.where("answerId").is(answerId));
        return mongoTemplate.count(query, Praise.class, Constant.Collection.PRAISE);
    }
}
