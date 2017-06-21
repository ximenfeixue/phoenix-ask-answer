package com.ginkgocap.ywxt.interlocution.dao.impl;

import com.ginkgocap.ywxt.interlocution.dao.AnswerMongoDao;
import com.ginkgocap.ywxt.interlocution.model.Answer;
import com.ginkgocap.ywxt.interlocution.model.Constant;
import com.ginkgocap.ywxt.interlocution.model.Question;
import com.ginkgocap.ywxt.interlocution.service.AskAnswerCommonService;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private static final int defaultSize = 1000;

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
        query.with(new Sort(Sort.Direction.DESC, Constant.TOP).and(new Sort(Sort.Direction.DESC, Constant.PRAISE_COUNT)).and(new Sort(Sort.Direction.ASC, Constant.CREATE_TIME)));
        if (size > 0) {
            query.skip(index);
            query.limit(size);
        }
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

    public Answer getAnswerByQuestionAndAnswererId(long questionId, long answererId) throws Exception {

        if (questionId < 0 || answererId < 0) {
            throw new IllegalArgumentException("questionId is error or answererId is error");
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("questionId").is(questionId));
        query.addCriteria(Criteria.where("answererId").is(answererId));
        return mongoTemplate.findOne(query, Answer.class, Constant.Collection.ANSWER);
    }

    public Answer getAnswerById(long id) throws Exception {

        if (id < 0) {
            throw new IllegalArgumentException("id is error");
        }
        Query query = new Query(Criteria.where(Constant._ID).is(id));

        return mongoTemplate.findOne(query, Answer.class, Constant.Collection.ANSWER);
    }

    public boolean updateAnswer(Answer answer) throws Exception {

        if (answer == null) {
            throw new IllegalArgumentException("answer is null!");
        }
        mongoTemplate.save(answer, Constant.Collection.ANSWER);
        return true;
    }

    public List<Answer> getAnswerByUId(long userId, int start, int size) throws Exception {

        if (userId < 0)
            throw new IllegalArgumentException("userId is error");
        Query query = new Query(Criteria.where(Constant.ANSWERER_ID).is(userId));
        // 查询 答案 所对应的问题 未被删除
        query.addCriteria(Criteria.where("status").is(0));
        long count = mongoTemplate.count(query, Answer.class, Constant.Collection.ANSWER);
        int index = start * size;
        if (index > count) {
            logger.error("because of index > count ! so return null! index :" + index + " size :" + size);
            return null;
        }
        query.with(new Sort(Sort.Direction.DESC, Constant.TOP).and(new Sort(Sort.Direction.DESC, Constant.CREATE_TIME)));
        query.skip(index);
        query.limit(size);
        return mongoTemplate.find(query, Answer.class, Constant.Collection.ANSWER);
    }

    public Answer getAnswerMaxPraiseCountByQId(long questionId) throws Exception {

        if (questionId < 0)
            throw new IllegalArgumentException("questionId is error");
        Query query = new Query(Criteria.where("questionId").is(questionId));
        query.with(new Sort(Sort.Direction.DESC, Constant.PRAISE_COUNT));
        query.limit(1);
        return mongoTemplate.findOne(query, Answer.class, Constant.Collection.ANSWER);
    }

    public boolean deleteAnswer(long id, long userId) throws Exception {

        if (id < 0 || userId < 0)
            throw new IllegalArgumentException("id < 0 or userId < 0 is error");
        Query query = new Query(Criteria.where(Constant._ID).is(id));
        query.addCriteria(Criteria.where("answererId").is(userId));
        Answer removeAnswer = mongoTemplate.findAndRemove(query, Answer.class, Constant.Collection.ANSWER);
        if (removeAnswer == null)
            logger.error("removeAnswer is not exist!");
        return removeAnswer != null;
    }

    public long countAnswerByUId(long userId) throws Exception {

        if (userId < 0)
            throw new IllegalArgumentException("userId < 0 param is error");
        Query query = new Query(Criteria.where("answererId").is(userId));

        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(new Criteria()), Aggregation.unwind("subStateList"), Aggregation.group("$subStateList.answererId"));
        AggregationResults<Answer> results = mongoTemplate.aggregate(aggregation, Constant.Collection.ANSWER, Answer.class);
        BasicDBList rawResults = (BasicDBList)results.getRawResults().get("result");
        List<Long> list = new ArrayList<Long>(rawResults.size());
        for (int i = 0; i < rawResults.size(); i++) {
            BasicDBObject obj = (BasicDBObject) rawResults.get(i);
            long answererId = obj.getLong("$subStateList.answererId");
            list.add(answererId);
        }
        return mongoTemplate.count(query, Answer.class, Constant.Collection.ANSWER);
    }

    public List<Long> getAnswererIdListSet() throws Exception {

        List<Answer> allList = mongoTemplate.findAll(Answer.class, Constant.Collection.ANSWER);
        Set<Long> set = new HashSet<Long>(defaultSize);
        for (Answer answer : allList) {
            long answererId = answer.getAnswererId();
            set.add(answererId);
        }
        List<Long> list = new ArrayList<Long>(set);
        return list;
    }

    public int countAnswerByQuestionId(long questionId) throws Exception {

        if (questionId < 0)
            throw new IllegalArgumentException("questionId is error");
        Query query = new Query(Criteria.where("questionId").is(questionId));
        long count = mongoTemplate.count(query, Answer.class, Constant.Collection.ANSWER);
        return (int)count;
    }

    public boolean deleteAnswerById(long id) throws Exception {

        if (id < 0)
            throw new IllegalArgumentException("id is error! ");
        Query query = new Query(Criteria.where(Constant._ID).is(id));
        Answer answer = mongoTemplate.findAndRemove(query, Answer.class);
        return answer != null;
    }

    public int countTopAnswerByQuestionId(long questionId) throws Exception {

        if (questionId < 0)
            throw new IllegalArgumentException("questionId is error");
        Query query = new Query(Criteria.where("questionId").is(questionId));
        query.addCriteria(Criteria.where(Constant.TOP).is(1));
        long count = mongoTemplate.count(query, Answer.class, Constant.Collection.ANSWER);
        return (int) count;
    }

    public List<Answer> searchAnswerByUser(List<Long> list, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception {

        Query query = new Query(Criteria.where(Constant.ANSWERER_ID).in(list));

        return search(query, startTime, endTime, timeSortType, praiseCountSortType, start, size);
    }

    public List<Answer> searchAnswerByQuestionIdList(List<Long> questionIdList, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception {

        Query query = new Query(Criteria.where("questionId").in(questionIdList));

        return search(query, startTime, endTime, timeSortType, praiseCountSortType, start, size);
    }

    public List<Answer> searchAnswerByContent(String keyword, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) throws Exception {

        Query query = new Query(Criteria.where("content").regex(".*?" + keyword + ".*"));

        return search(query, startTime, endTime, timeSortType, praiseCountSortType, start, size);
    }

    public boolean batchUpdateAnswerStatus(long questionId) {

        Query query = new Query(Criteria.where("questionId").is(questionId));
        Update update = new Update();
        update.set("status", (byte) 1);
        WriteResult writeResult = mongoTemplate.updateMulti(query, update, Answer.class);
        return writeResult.getN() > 0;
    }

    public List<Answer> getAllAnswer(int start, int size) {

        if (start < 0 || size < 0)
            throw new IllegalArgumentException("start or size param is error");
        int index = 0;
        index = start * size;
        Query query = new Query();
        query.skip(index);
        query.limit(size);
        return mongoTemplate.find(query, Answer.class, Constant.Collection.ANSWER);
    }

    private List<Answer> search(Query query, long startTime, long endTime, byte timeSortType, byte praiseCountSortType, int start, int size) {

        if (startTime > 0 && endTime > 0) {
            query.addCriteria(Criteria.where(Constant.CREATE_TIME).gte(startTime).lte(endTime));
        }
        long count = mongoTemplate.count(query, Answer.class, Constant.Collection.ANSWER);
        int index = start * size;
        if (index > count) {
            logger.info("because of index > count , so return null!");
            return null;
        }
        if (index + size > count) {
            size = (int) count - index;
        }
        query.with(new Sort(Sort.Direction.DESC, Constant.TOP));
        // 时间 排序
        if (timeSortType == 0) {
            query.with(new Sort(Sort.Direction.DESC, Constant.CREATE_TIME));
        } else if (timeSortType == 1) {
            query.with(new Sort(Sort.Direction.ASC, Constant.CREATE_TIME));
        } else {}
        // 点赞数 排序
        if (praiseCountSortType == 0) {
            query.with(new Sort(Sort.Direction.DESC, Constant.PRAISE_COUNT));
        } else if (praiseCountSortType == 1) {
            query.with(new Sort(Sort.Direction.ASC, Constant.PRAISE_COUNT));
        } else {}
        query.skip(index);
        query.limit(size);
        return mongoTemplate.find(query, Answer.class, Constant.Collection.ANSWER);
    }
}
