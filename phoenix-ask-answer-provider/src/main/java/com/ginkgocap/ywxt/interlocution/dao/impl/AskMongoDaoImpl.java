package com.ginkgocap.ywxt.interlocution.dao.impl;

import com.ginkgocap.ywxt.interlocution.dao.AskMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.service.AskAnswerCommonService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * Created by Wang fei on 2017/5/24.
 */
@Repository("askMongoDaoImpl")
public class AskMongoDaoImpl implements AskMongoDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private AskAnswerCommonService interlocutionCommonService;

    private final String collectionName = "question";

    public Question insert(Question question) throws Exception{

        if (question == null) {
            throw new IllegalArgumentException("question is null");
        }
        question.setId(interlocutionCommonService.getInterlocutionSequenceId());
        question.setCreateTime(System.currentTimeMillis());
        question.setUpdateTime(System.currentTimeMillis());
        mongoTemplate.insert(question, collectionName);
        return question;

    }
}
