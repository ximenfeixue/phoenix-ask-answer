package com.ginkgocap.ywxt.interlocution.dao.impl;

import com.ginkgocap.ywxt.interlocution.dao.AskMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Constant;
import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.model.QuestionCollect;
import com.ginkgocap.ywxt.interlocution.model.QuestionReport;
import com.ginkgocap.ywxt.interlocution.service.AskAnswerCommonService;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
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
        query.addCriteria(Criteria.where("disabled").is(0));
        long count = mongoTemplate.count(query, Question.class, Constant.Collection.QUESTION);
        int index = start * size;
        if (index > count) {
            logger.error("index > total , so return null!" + "start: " + start + "size: " + size + "count :" + count);
            return null;
        }
        if (start + size > count) {
            size = (int)count - start;
        }
        query.with(new Sort(Sort.Direction.DESC, Constant.TOP).and(new Sort(Sort.Direction.DESC, Constant.CREATE_TIME)));
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

    public boolean updateStatusAndAnswerCount(long id, byte status, int answerCount) throws Exception {

        Query query = new Query(Criteria.where(Constant._ID).is(id));
        Update update = new Update();
        update.set("status", status);
        update.set("answerCount", answerCount);
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
        long count = countQuestionByUId(userId);
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

    public long countQuestionByUId(long userId) {

        if (userId < 0)
            throw new IllegalArgumentException("param is error");
        Query query = new Query(Criteria.where(Constant.USER_ID).is(userId));
        return mongoTemplate.count(query, Question.class, Constant.Collection.QUESTION);
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
        long count = countQuestionCollectByUId(userId);
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

    public long countQuestionCollectByUId(long userId) {

        if (userId < 0)
            throw new IllegalArgumentException("userId < 0 is error");
        Query query = new Query(Criteria.where(Constant.USER_ID).is(userId));
        return mongoTemplate.count(query, QuestionCollect.class, Constant.Collection.QUESTION_COLLECT);
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

    public boolean deleteQuestion(long id, long userId) throws Exception {

        if (id < 0 || userId < 0)
            throw new IllegalArgumentException("id < 0 or userId < 0 is error");
        Query query = new Query(Criteria.where(Constant._ID).is(id));
        query.addCriteria(Criteria.where(Constant.USER_ID).is(userId));
        Question removeQuestion = mongoTemplate.findAndRemove(query, Question.class, Constant.Collection.QUESTION);
        if (removeQuestion == null)
            logger.error("removeQuestion is not exist!");
        return removeQuestion != null;
    }

    public Long getReadCount(long id) throws Exception {

        if (id < 0)
            throw new IllegalArgumentException("id < 0 param is error");
        Question question = mongoTemplate.findById(id, Question.class, Constant.Collection.QUESTION);
        return question != null ? question.getReadCount() : null;
    }

    public boolean addTop(long id) throws Exception {

        if (id < 0)
            throw new IllegalArgumentException("id < 0 param is error");
        Query query = new Query(Criteria.where(Constant._ID).is(id));
        Update update = new Update();
        update.set("top", (byte) 1 );
        Question question = mongoTemplate.findAndModify(query, update, Question.class);
        return question != null;
    }

    public boolean deleteTop(long id) throws Exception {

        if (id < 0)
            throw new IllegalArgumentException("id < 0 param is error");
        Query query = new Query(Criteria.where(Constant._ID).is(id));
        Update update = new Update();
        update.set("top", (byte) 0);
        Question question = mongoTemplate.findAndModify(query, update, Question.class);
        return question != null;
    }

    public List<Question> getAllQuestion(int start, int size) throws Exception {

        if (start < 0 || size < 0)
            throw new IllegalArgumentException("start or size param is error");
        int index = 0;
        index = start * size;
        Query query = new Query();
        query.skip(index);
        query.limit(size);
        return mongoTemplate.find(query, Question.class, Constant.Collection.QUESTION);
    }

    public boolean updateQuestionAnswerCount(long id, int count) throws Exception {

        if (id < 0 || count < 0)
            throw new IllegalArgumentException("id or count param is error");
        Query query = new Query(Criteria.where(Constant._ID).is(id));
        Update update = new Update();
        update.set("answerCount", count);
        Question question = mongoTemplate.findAndModify(query, update, Question.class);
        return question != null;
    }

    public boolean updateDisabled(byte disabled, long questionId) throws Exception {

        if (disabled < 0 || questionId < 0)
            throw new IllegalArgumentException("disabled or question param is error");
        Query query = new Query(Criteria.where(Constant._ID).is(questionId));
        Update update = new Update();
        update.set("disabled", disabled);
        Question question = mongoTemplate.findAndModify(query, update, Question.class);
        return question != null;
    }

    public List<Question> searchQuestionByUser(List<Long> userIdList, long startTime, long endTime, byte status, byte timeSortType, byte readCountSortType, byte answerCountSortType, int start, int size) {

        Query query = new Query(Criteria.where(Constant.USER_ID).in(userIdList));

        return search(query, startTime, endTime, status, timeSortType, readCountSortType, answerCountSortType, start, size);
    }

    public long countQuestionByUser(List<Long> userIdList, long startTime, long endTime, byte status) {

        Query query = new Query(Criteria.where(Constant.USER_ID).in(userIdList));
        if (startTime > 0 && endTime > 0) {
            query.addCriteria(Criteria.where(Constant.CREATE_TIME).gte(startTime).lte(endTime));
        }
        if (status < 2 && status > -1) {
            query.addCriteria(Criteria.where("disabled").is(status));
        }
        return mongoTemplate.count(query, Question.class, Constant.Collection.QUESTION);
    }

    public List<Question> searchQuestionByTitle(String keyword, long startTime, long endTime, byte status, byte timeSortType, byte readCountSortType, byte answerCountSortType, int start, int size) {

        Query query = null;
        if (keyword != null && !"null".equals(keyword)) {
            query = new Query(Criteria.where("title").regex(".*?" + keyword + ".*"));
        } else {
            query = new Query();
        }
        return search(query, startTime, endTime, status, timeSortType, readCountSortType, answerCountSortType, start, size);
    }

    public long countQuestionByTitle(String keyword, long startTime, long endTime, byte status) {

        Query query = null;
        if (keyword != null && !"null".equals(keyword)) {
            query = new Query(Criteria.where("title").regex(".*?" + keyword + ".*"));
        } else {
            query = new Query();
        }
        if (startTime > 0 && endTime > 0) {
            query.addCriteria(Criteria.where(Constant.CREATE_TIME).gte(startTime).lte(endTime));
        }
        if (status < 2 && status > -1) {
            query.addCriteria(Criteria.where("disabled").is(status));
        }
        return mongoTemplate.count(query, Question.class, Constant.Collection.QUESTION);
    }
    // 未完待续
    public List getCreateQuestionByUserId(long userId, long startTime, long endTime) {

        Query query = new Query();
        query.addCriteria(Criteria.where(Constant.USER_ID).is(userId));
        if (startTime > 0 && endTime > 0) {
            query.addCriteria(Criteria.where(Constant.CREATE_TIME).gte(startTime).lte(endTime));
        }
        GroupOperation groupOperation = Aggregation.group("ad_id", "ad_title").sum("clicknum").as("clicksum");

        GroupBy groupBy = GroupBy.keyFunction(Constant.CREATE_TIME).initialDocument("{count:0}").reduceFunction("function(doc, out){out.count++}")
                .finalizeFunction("function(out){return out;}");
        GroupByResults<Question> res = mongoTemplate.group(Constant.Collection.QUESTION, groupBy, Question.class);
        DBObject obj = res.getRawResults();
        BasicDBList dbList = (BasicDBList) obj.get("retval");


        return null;
    }

    private List<Question> search(Query query, long startTime, long endTime, byte status, byte timeSortType, byte readCountSortType, byte answerCountSortType, int start, int size) {

        if (startTime > 0 && endTime > 0) {
            query.addCriteria(Criteria.where(Constant.CREATE_TIME).gte(startTime).lte(endTime));
        }
        if (status < 2 && status > -1) {
            query.addCriteria(Criteria.where("disabled").is(status));
        }
        long count = mongoTemplate.count(query, Question.class, Constant.Collection.QUESTION);
        int index = start * size;
        if (index > count) {
            logger.info("because of index > count, so return null, skip query.");
            return null;
        }
        if (index + size > count) {
            size = (int)count - index;
        }
        query.with(new Sort(Sort.Direction.DESC, Constant.TOP));
        // 发布时间 排序
        if (timeSortType == 0) {
            query.with(new Sort(Sort.Direction.DESC, Constant.CREATE_TIME));
        } else if (timeSortType == 1) {
            query.with(new Sort(Sort.Direction.ASC, Constant.CREATE_TIME));
        } else {}
        // 阅读数 排序
        if (readCountSortType == 0) {
            query.with(new Sort(Sort.Direction.DESC, "readCount"));
        } else if (readCountSortType == 1) {
            query.with(new Sort(Sort.Direction.ASC, "readCount"));
        } else {}
        // 回答数 排序
        if (answerCountSortType == 0) {
            query.with(new Sort(Sort.Direction.DESC, "answerCount"));
        } else if (answerCountSortType == 1) {
            query.with(new Sort(Sort.Direction.ASC, "answerCount"));
        } else {}
        if (start >= 0) {
            query.skip(index);
            query.limit(size);
        }
        return mongoTemplate.find(query, Question.class, Constant.Collection.QUESTION);
    }
}
