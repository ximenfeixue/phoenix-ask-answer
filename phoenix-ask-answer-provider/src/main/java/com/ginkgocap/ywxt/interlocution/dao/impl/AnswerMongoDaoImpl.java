package com.ginkgocap.ywxt.interlocution.dao.impl;

import com.ginkgocap.ywxt.interlocution.dao.AnswerMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Answer;
import com.ginkgocap.ywxt.interlocution.model.Constant;
import com.ginkgocap.ywxt.interlocution.service.AskAnswerCommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/27.
 */
@Repository("answerMongoDao")
public class AnswerMongoDaoImpl implements AnswerMongoDao {

    private final Logger logger = LoggerFactory.getLogger(AnswerMongoDaoImpl.class);

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    private AskAnswerCommonService askAnswerCommonService;

    public List<Answer> getAnswerListByQuestionId(long questionId, int start, int size) {


        Query query = new Query(Criteria.where("questionId").is(questionId));
        long count = mongoTemplate.count(query, Answer.class, Constant.Collection.ANSWER);
        int index = start * size;
        if (index > count) {
            logger.error("index > count . so return null!" + "start:" + start + "size:" + size + "count :" + count);
            return null;
        }
        if (index + size > count) {
            size = (int)count - index;
        }
        query.with(new Sort(Sort.Direction.DESC, Constant.TOP).and(new Sort(Sort.Direction.DESC, Constant.PRAISE_COUNT)).and(new Sort(Sort.Direction.DESC, Constant.CREATE_TIME)));
        query.skip(index);
        query.limit(size);
        return mongoTemplate.find(query, Answer.class, Constant.Collection.ANSWER);
    }

    public Answer insert(Answer answer) throws Exception {

        if (answer == null)
            throw new IllegalArgumentException("answer is null!");
        answer.setId(askAnswerCommonService.getInterlocutionSequenceId());
        answer.setCreateTime(System.currentTimeMillis());
        answer.setUpdateTime(System.currentTimeMillis());
        mongoTemplate.insert(answer, Constant.Collection.ANSWER);
        return answer;
    }
}
