package com.ginkgocap.ywxt.interlocution.web.controller;

import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.ginkgocap.ywxt.cache.Cache;
import com.ginkgocap.ywxt.interlocution.model.*;
import com.ginkgocap.ywxt.interlocution.service.AnswerService;
import com.ginkgocap.ywxt.interlocution.service.AskService;
import com.ginkgocap.ywxt.interlocution.service.PraiseService;
import com.ginkgocap.ywxt.user.model.User;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseController {

	private final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

	/*@Resource
	private RedisCacheService redisCacheService;
*/

	@Resource
	private Cache cache;

	@Autowired
	private PraiseService praiseService;

	@Autowired
	private AskService askService;

	@Autowired
	private AnswerService answerService;

	/**
	 * 从body中获得参数
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected String getBodyParam(HttpServletRequest request) throws Exception {
		BufferedReader reader=null;
		try{
		 reader = request.getReader();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		if(reader==null){
			return "不能为空！";
		}
		String line = null;
		StringBuffer jsonIn = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			jsonIn.append(line);
		}
		String sjson = jsonIn.toString();
		LOGGER.info(sjson);
		return sjson;

	}
	/*protected User getUser(HttpServletRequest request) throws Exception {
		String key = UserUtil.getUserSessionKey(request);
		User user = (User) redisCacheService.getRedisCacheByKey(key);
		return user;
	}*/

	protected String getJsonIn(HttpServletRequest request) throws IOException {
		String requestJson=(String)request.getAttribute("requestJson");
		if(requestJson==null){
			return "";
		}
		return requestJson;
	}

	protected String getJsonParamStr(HttpServletRequest request)
			throws IOException {
		String result = getJsonIn(request);
		return result;
	}
	protected User getJTNUser(HttpServletRequest request) {
		User user = getUser(request);

		if(null == user){
			user = new User();
			user.setId(0);//金桐脑
			return user;
		}
		return user;
	}

	protected User getYINUser(HttpServletRequest request) {

		User user = null;
		user = this.getUser(request);
		if (user == null || user.getId() != 1) {
			return null;
		}
		return user;
	}

	protected boolean isWeb(HttpServletRequest request)
	{
		String s = request.getHeader("s");
		return "web".equals(s);
	}

	/**
	 * 获取用户
	 * @param request
	 * @return
	 */
	public User getUser(HttpServletRequest request) {
		//在AppFilter过滤器里面从cache获取了当前用户对象并设置到request中了
		return (User) request.getAttribute("sessionUser");
	}

	public Long getUserId(HttpServletRequest request){
		User user=this.getUser(request);
		Long uid=0l;
		if(user!=null){
			uid=user.getId();
		}
		return uid;
	}

	public static SimpleFilterProvider assoFilterProvider(final String className) {
		Set<String> filter = new HashSet<String>(8); //this number must be increased by fields
		filter.add("id"); // id',
		filter.add("appId");
		filter.add("assocTypeId");
		filter.add("assocDesc"); // '关联描述，比如文章的作者，或者编辑等；关联标签描述',
		filter.add("assocTypeId"); // '被关联的类型可以参考AssociateType对象，如：知识, 人脉,组织，需求，事件等',
		filter.add("assocId"); // '被关联数据ID',
		filter.add("assocTitle"); // '被关联数据标题',
		filter.add("assocMetadata"); // '被关联数据的的摘要用Json存放，如图片，连接URL定义等',

		return simpleFilterProvider(className, filter);
	}

	public static SimpleFilterProvider simpleFilterProvider(final String className, final Set<String> filter) {
		if (filter != null && filter.size() > 0) {
			SimpleFilterProvider filterProvider = new SimpleFilterProvider();
			filterProvider.addFilter(className, SimpleBeanPropertyFilter.filterOutAllExcept(filter));
			return filterProvider;
		}
		else {
			return null;
		}
	}
	protected PartAnswer convertAnswer(Answer answer) {

		PartAnswer partAnswer = new PartAnswer();
		partAnswer.setAnswererId(answer.getAnswererId());
		partAnswer.setAnswerId(answer.getId());
		partAnswer.setContent(answer.getContent());
		partAnswer.setPraiseCount(answer.getPraiseCount());
		partAnswer.setType(answer.getType());
		partAnswer.setVirtual(answer.getVirtual());
		partAnswer.setTop(answer.getTop());
		partAnswer.setJtFile(answer.getJtFile());
		return partAnswer;
	}

	/**
	 * 将 点赞者id 放到 redis中
	 * 不设置 过期时间
	 * @param answerId 作为key
	 * @param userId 作为 value
	 */
	protected int addPraiseUId2Redis(long answerId, long userId) {

		Set<String> set = cache.smembersRedis("ask_answer_praise_" + answerId);
		Answer answer = null;
		int praiseCount = 0;
		if (CollectionUtils.isEmpty(set)) {
			try {
				answer = answerService.getAnswerById(answerId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (answer != null) {
				praiseCount = answer.getPraiseCount();
				praiseCount = answer.addPraiseCount(praiseCount);
			}
			cache.saddRedis("ask_answer_praise_" + answerId, -1, "" + userId);
			return praiseCount;
		}
		cache.saddRedis("ask_answer_praise_" + answerId, -1, "" + userId);
		set = cache.smembersRedis("ask_answer_praise_" + answerId);
		return set.size();
	}

	/**
	 * 查询点赞者 set 通过 答案 id
	 *
	 * 暂时 主要是 为了 返回前端的 点赞数 praiseCount 通过 set.size();
	 * @param answerId
	 * @return
	 */
	protected Set<String> getPraiseUIdSet(long answerId) {

		Set<String> set = cache.smembersRedis("ask_answer_praise_" + answerId);
		/*if (CollectionUtils.isEmpty(set)) {
			// 同步数据 放到 一个 task 中执行
			updateRedisByMongo(answerId);
			set = cache.smembersRedis("ask_answer_praise_" + answerId);
		}*/
		return set;
	}

	/**
	 * 判断 用户 id 是否 在答案的点赞 set 中
	 * @param answerId
	 * @param userId
	 * @return
	 */
	protected boolean isExistPraise(long answerId, long userId) {

		boolean existPraise;
		existPraise = cache.sismemberRedis("ask_answer_praise_" + answerId , "" + userId);
		if (!existPraise) {
			Set<String> set = cache.smembersRedis("ask_answer_praise_" + answerId);
			if (CollectionUtils.isNotEmpty(set)) {
				return false;
			}
			// 同步数据
			//updateRedisByMongo(answerId);
			existPraise = cache.sismemberRedis("ask_answer_praise_" + answerId, "" + userId);
		}
		return existPraise;
	}

	/**
	 * 删除 取消 点赞的人
	 * @param answerId
	 * @param userId
	 * @return
	 */
	protected long removePraiseUId(long answerId, long userId) {

		return cache.sremRedis("ask_answer_praise_" + answerId, "" + userId);
	}

	/**
	 * 若redis 中 key 失效 ，去mongo 中查询 再同步数据到 redis 中
	 * @param answerId
	 */
	protected void updateRedisByMongo(long answerId) {

		int start = 0;
		final int size = 10;
		// 加 size
		long count = 0;
		try {
			count = praiseService.countByAnswerId(answerId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<String> list = new ArrayList<String>((int)count);
		// 放到 一个 单独 task 进行操作 redis
		try {
			List<Praise> praiseUserList = praiseService.getPraiseUser(answerId, start++, size);
			while (CollectionUtils.isNotEmpty(praiseUserList)) {
				for (Praise praise : praiseUserList) {
					long admirerId = praise.getAdmirerId();
					list.add("" + admirerId);
				}
				praiseUserList = praiseService.getPraiseUser(answerId, start++, size);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (CollectionUtils.isNotEmpty(list) ) {
			cache.saddRedis("ask_answer_praise_" + answerId, 24 * 60 * 60, list.toArray(new String [1]));
		}
	}

	/**
	 * 从 redis 中获得 answerCount 若失效，则从数据库同步到redis中
	 * 保证返回真实 answerCount
	 * @param id
	 * @return
	 */
	protected int getAnswerCountByRedis(long id) {

		/*List<Answer> answerList = null;
		List<String> list = new ArrayList<String>();
		Set<String> set = cache.smembersRedis("ask_answer_answerCount_" + id);
		int answerCount = 0;
		if (CollectionUtils.isEmpty(set)) {
			try {
				// 用 answer service 通过 questionId查询 个数
				answerList = answerService.getAnswerListByQuestionId(id, 0, 20);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (CollectionUtils.isNotEmpty(answerList)) {
				for (Answer answer : answerList) {
					if (answer == null)
						continue;
					long answerId = answer.getId();
					list.add("" + answerId);
				}
				answerCount = answerList.size();
				cache.saddRedis("ask_answer_answerCount_" + id, 24 * 60 * 60, list.toArray(new String[1]));
			}

			if (answerCount < 1) {
				return 0;
			}
			*//*boolean flag = cache.setByRedis("ask_answer_answerCount_" + id, answerCount, 24 * 60 * 60);
			if (!flag) {
				LOGGER.error("invoke redis failed! please check redis server");
			}*//*
			return answerCount;
		}
		return set.size();*/
		Question question = null;
		int count = 0;
		Long answerCount = (Long)cache.getByRedis("ask_answer_answerCount_" + id);
		if (null == answerCount || answerCount.longValue() < 1) {
			try {
				question = askService.getQuestionById(id);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (question != null) {
				count = question.getAnswerCount();
			}
			answerCount = Long.valueOf(count);
			boolean flag = cache.setByRedis("ask_answer_answerCount_" + id, answerCount, 24 * 60 * 60);
			if (!flag) {
				LOGGER.error("redis set failed! id:" + "ask_answer_answerCount_" + id);
			}
		}
		return Integer.valueOf(answerCount.toString());
	}

	/**
	 * 添加 答案数  自增 +1
	 * @param id
	 * @return
	 */
	protected long addAnswerCountByRedis(long id) {

		/*long answerCount = 0;
		Set<String> set = cache.smembersRedis("ask_answer_answerCount_" + id);
		if (CollectionUtils.isNotEmpty(set)) {
			cache.saddRedis("ask_answer_answerCount_" + id, 24 * 60 * 60, "" + answerId);
			set = cache.smembersRedis("ask_answer_answerCount_" + id);
			answerCount = set.size();
		} else {
			answerCount = getAnswerCountByRedis(id);
		}
		return answerCount;*/
		// 保证 redis 中 key 不失效
		int answerCount = getAnswerCountByRedis(id);
		return cache.incr("ask_answer_answerCount_" + id);

	}
	/**
	 * 减少 答案数  自增 -1
	 * @param id
	 * @return
	 */
	protected long minusAnswerCountByRedis(long id) {

		/*long answerCount = 0;
		Set<String> set = cache.smembersRedis("ask_answer_answerCount_" + id);
		if (CollectionUtils.isNotEmpty(set)) {
			cache.sremRedis("ask_answer_answerCount_" + id, "" + answerId);
			set = cache.smembersRedis("ask_answer_answerCount_" + id);
			answerCount = set.size();
		} else {
			answerCount = getAnswerCountByRedis(id);
		}
		return answerCount < 0 ? 0 : answerCount;*/
		// 保证 redis 中 key 不失效
		int answerCount = getAnswerCountByRedis(id);
		return cache.decr("ask_answer_answerCount_" + id);
	}
	//abstract Logger logger();
}
