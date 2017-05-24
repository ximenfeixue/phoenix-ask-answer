package com.ginkgocap.ywxt.interlocution.web.filter;

import com.ginkgocap.ywxt.cache.Cache;
import com.ginkgocap.ywxt.interlocution.model.Constant;
import com.ginkgocap.ywxt.interlocution.utils.CommonUtil;
import com.ginkgocap.ywxt.interlocution.utils.MyStringUtils;
import com.ginkgocap.ywxt.interlocution.utils.RedisKeyUtils;
import com.ginkgocap.ywxt.user.model.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AppFilter implements Filter {
	private Logger logger = LoggerFactory.getLogger(AppFilter.class);
	
	String excludedUrl = "";
	String[] excludedUrlArray = {};//任何情况都不需要登录，在web.xml里面配置
	//允许游客状态的接口
	String[] webExcludedUrl = { "mobileApp/getVCodeForPassword.json",
			"mobileApp/setNewPassword.json","register/pwd/reset.json",
			"org/dynamicNews/dynamicNewsList.json","dynamicNews/getDynamicComment",
			"person/peopleHomeList.json","person/getPeopleDetail.json","code/peopleCodeList.json",
			"code/peopleCodeListByName.json","get/area.json","/dynamicNews/getListDynamicNews.json","/register/sendValidateEmail.json",
			"person/getPeopleTemplate.json","/register/isExistByMobileOrByEmail.json","/knowledge/getKnowledgeDetails.json","/file/nginx/downloadFJ","/org/orgAndProInfo.json","/customer/findCusProfile.json","/demand/getDemandDetail.json","/demand/findDemandFile.json",
			"/webknowledge/home/separate.json","/webknowledge/home/getAggregationRead.json","/webknowledge/home/getHotTag.json","/webknowledge/home/getRecommendedKnowledge.json",
			"/webknowledge/getKnowledgeByColumnAndSource.json","/webknowledge/queryMore.json","/webknowledge/home/getHotList.json","/webknowledge/home/getCommentList.json",
		   "set/checkMailByStatus.json","/register/checkOrganRegister.json","metadata/search.json","get/code.json","/knowledge/getKnowledgeComment.json","/org/getDiscoverList.json","/resource/hotList.json",
		   "/register/checkIdentifyCode.json","/demand/getDemandList.json","/demandComment/getDemandCommentList.json","/file/nginx/downloadFJ2"};

	@Override
	public void destroy() { 

	}

	private User getUser(HttpServletRequest request) {
		// 判断客户端请求方式
		if (CommonUtil.getRequestIsFromWebFlag()) {
			String sessionId = request.getHeader("sessionID");
			if (StringUtils.isNotBlank(sessionId)) {
				String key = RedisKeyUtils.getSessionIdKey(sessionId);
				return getUser(request, key);
			}
		} else {
			String sessionId = request.getHeader("sessionID");
			if (sessionId != null && !"null".equals(sessionId)
					&& !"".equals(sessionId)) {
				String key = "user" + sessionId;
				return getUser(request, key);
			}
		}
		return null;
	}

	/**
	 * 获取用户信息
	 * 
	 * @param request
	 *            request
	 * @param key
	 *            sessionId key
	 * @return user
	 * @author haiyan
	 */
	private User getUser(HttpServletRequest request, String key) {
		WebApplicationContext wac = WebApplicationContextUtils
				.getWebApplicationContext(request.getSession()
						.getServletContext());
		Cache cache = (Cache) wac.getBean("cache");
		User user = (User) cache.getByRedis(key);
		if (user != null) {
			cache.setByRedis(key, user, 60 * 60 * 24);
		}
		return user;
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String s = req.getHeader("s");
		logger.info("params s is {}", s);
		boolean requestIsFromWeb = Constant.WEB.equals(s);
		if (requestIsFromWeb) {
			CommonUtil.setRequestIsFromWebFlag(true);
		}

		Long userId = 0L;
		User user = getUser(req);
		if (null != user && user.getId() > 0) {
			userId = user.getId();
			request.setAttribute("sessionUser", user);
		}

		String url = req.getRequestURI();
		// cookies不为空，则清除
		if (url.contains("file/") || url.contains("update/bindEmailStatus")
				|| url.contains("/organ/uploadIdCardImg.json")) {
			chain.doFilter(request, response);
			CommonUtil.setRequestIsFromWebFlag(false);
			return;
		}
		boolean loginFlag = true;
		for (String excludedUrl : excludedUrlArray) {
			if (url.contains(MyStringUtils.replaceSpecial(excludedUrl))) {
				loginFlag = false;
				break;
			}
		}		

		if (requestIsFromWeb) {
			for (String excludedUrl : webExcludedUrl) {
				if (url.contains(excludedUrl)) {
					loginFlag = false;
					break;
				}
			}
		}

		chain.doFilter(request, response);
		CommonUtil.setRequestIsFromWebFlag(false);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		excludedUrl = config.getInitParameter("excludedUrl");
		if (!StringUtils.isNotBlank(excludedUrl)) {
			excludedUrlArray = excludedUrl.split(",");
		}
	}
	/*
	 * public void setUser(HttpServletRequest request){ String
	 * sessionId=request.getHeader("sessionID"); if(sessionId!=null &&
	 * !"null".equals(sessionId) && !"".equals(sessionId)){
	 * WebApplicationContext wac=
	 * WebApplicationContextUtils.getWebApplicationContext
	 * (request.getSession().getServletContext()); Cache cache=(Cache)
	 * wac.getBean("cache"); User user=(User)
	 * cache.getByRedis("user"+sessionId); if(user!=null){
	 * cache.setByRedis("user"+sessionId, user ,60 * 60 * 24);//设定过期日期为1天
	 * 如需修改可滞后 } } }
	 */
}
