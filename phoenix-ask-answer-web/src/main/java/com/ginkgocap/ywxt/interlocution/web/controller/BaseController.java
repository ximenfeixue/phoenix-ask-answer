package com.ginkgocap.ywxt.interlocution.web.controller;

import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.ginkgocap.ywxt.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class BaseController {

	private final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);

	/*@Resource
	private RedisCacheService redisCacheService;
*/
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

}
