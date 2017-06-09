package com.ginkgocap.ywxt.interlocution.dao.impl;

import com.ginkgocap.ywxt.interlocution.dao.AskMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Constant;
import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.model.QuestionCollect;
import com.ginkgocap.ywxt.interlocution.model.QuestionReport;
import com.ginkgocap.ywxt.interlocution.service.AskAnswerCommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Wang fei on 2017/5/24.
 */
@Repository("askMongoDao")
public class AskMongoDaoImpl implements AskMongoDao {

    private final Logger logger = LoggerFactory.getLogger(AskMongoDaoImpl.class);

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    private AskAnswerCommonService askAnswerCommonService;

    // 已回答状态
    private static final byte ANSWER_STATUS = 1;

    public Question insert(Question question) throws Exception{

        if (question == null) {
            throw new IllegalArgumentException("question is null");
        }
        question.setId(askAnswerCommonService.getInterlocutionSequenceId());
        question.setCreateTime(System.currentTimeMillis());
        question.setUpdateTime(System.currentTimeMillis());
        mongoTemplate.insert(question, Constant.Collection.QUESTION);
        return question;

    }

    public List<Question> getAllAskAnswerByStatus(byte status, int start, int size) throws Exception{

        Criteria criteria = null;
        if (status < 0) {
            criteria = new Criteria();
        } else {
            // 查询 未回答 已回答
            criteria = Criteria.where("status").is(status);
        }
        Query query = new Query(criteria);
        long count = mongoTemplate.count(query, Question.class, Constant.Collection.QUESTION);
        int index = start * size;
        if (index > count) {
            logger.error("index > total , so return null!" + "start: " + start + "size: " + size + "count :" + count);
            return null;
        }
        if (start + size > count) {
            size = (int)count - start;
        }
        query.with(new Sort(Sort.Direction.DESC, Constant.TOP).and(new Sort(Sort.Direction.DESC, Constant.READ_COUNT).and(new Sort(Sort.Direction.ASC, Constant.CREATE_TIME))));
        query.skip(index);
        query.limit(size);
        return mongoTemplate.find(query, Question.class, Constant.Collection.QUESTION);
    }

    public Question getQuestionById(long id) throws Exception {

        Query query = new Query(Criteria.where(Constant._ID).is(id));

        return mongoTemplate.findOne(query, Question.class, Constant.Collection.QUESTION);
    }

    public boolean updateStatus(long id) throws Exception {

        Query query = new Query(Criteria.where(Constant._ID).is(id));
        Update update = new Update();
        update.set("status", ANSWER_STATUS);
        Question question = mongoTemplate.findAndModify(query, update, Question.class, Constant.Collection.QUESTION);
        return question != null;
    }

    public void update(Question question) throws Exception {

        if (question == null) {
            throw new IllegalArgumentException("question is null");
        }
        mongoTemplate.save(question, Constant.Collection.QUESTION);
    }

    public List<Question> getQuestionByUId(long userId, int start, int size) throws Exception {

        if (userId < 0 || start < 0 || size < 0)
            throw new IllegalArgumentException("param is error");
        Query query = new Query(Criteria.where(Constant.USER_ID).is(userId));
        long count = mongoTemplate.count(query, Question.class, Constant.Collection.QUESTION);
        int index = start * size;
        if (index > count) {
            logger.error("because of index > count , so return null. index :" + index + " count :" + count);
            return null;
        }
        query.with(new Sort(Sort.Direction.DESC, Constant.TOP).and(new Sort(Sort.Direction.DESC, Constant.CREATE_TIME)));
        query.skip(index);
        query.limit(size);
        return mongoTemplate.find(query, Question.class, Constant.Collection.QUESTION);
    }

    public QuestionCollect addCollect(QuestionCollect collect) throws Exception {

        if (collect == null)
            throw new IllegalArgumentException("collect is null");
        collect.setId(askAnswerCommonService.getInterlocutionSequenceId());
        collect.setCreateTime(System.currentTimeMillis());
        mongoTemplate.insert(collect, Constant.Collection.QUESTION_COLLECT);
        return collect;
    }

    public boolean deleteCollect(long questionId, long userId) throws Exception {

        if (questionId < 0)
            throw new IllegalArgumentException("questionId is error");
        Query query = new Query(Criteria.where("questionId").is(questionId));
        query.addCriteria(Criteria.where(Constant.USER_ID).is(userId));
        QuestionCollect collect = mongoTemplate.findAndRemove(query, QuestionCollect.class, Constant.Collection.QUESTION_COLLECT);
        return collect != null;
    }

    public QuestionReport addReport(QuestionReport report) throws Exception {

        if (report == null)
            throw new IllegalArgumentException("report is null");
        report.setId(askAnswerCommonService.getInterlocutionSequenceId());
        report.setCreateTime(System.currentTimeMillis());
        mongoTemplate.insert(report, Constant.Collection.QUESTION_REPORT);
        return report;
    }

    public List<QuestionCollect> getCollectByUId(long userId, int start, int size) throws Exception {

        if (userId < 0 || start < 0 || size < 0)
            throw new IllegalArgumentException("param is error");

        Query query = new Query(Criteria.where(Constant.USER_ID).is(userId));
        long count = mongoTemplate.count(query, QuestionCollect.class, Constant.Collection.QUESTION_COLLECT);
        int index = start * size;
        if (index > count) {
            logger.error("because of index > count , so return null .index :" + index + "count :" + count);
            return null;
        }
        query.with(new Sort(Sort.Direction.DESC, Constant.CREATE_TIME));
        query.skip(index);
        query.limit(size);
        return mongoTemplate.find(query, QuestionCollect.class, Constant.Collection.QUESTION_COLLECT);
    }

    public QuestionCollect getCollectByUIdQuestionId(long userId, long questionId) throws Exception {

        if (userId < 0 || questionId < 0)
            throw new IllegalArgumentException("userId or questionId is error");

        Query query = new Query(Criteria.where(Constant.USER_ID).is(userId));
        query.addCriteria(Criteria.where("questionId").is(questionId));
        return mongoTemplate.findOne(query, QuestionCollect.class, Constant.Collection.QUESTION_COLLECT);
    }

    public boolean deleteQuestion(long id) throws Exception {

        if (id < 0)
            throw new IllegalArgumentException("id < 0 is error");
        Query query = new Query(Criteria.where(Constant._ID).is(id));
        Question removeQuestion = mongoTemplate.findAndRemove(query, Question.class, Constant.Collection.QUESTION);
        return removeQuestion != null;
    }

    /*public Question addQuestionReadCount(Question question) {

        Update update = new Update();
        update.set("")
    }*/
}
