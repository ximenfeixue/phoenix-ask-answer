package com.ginkgocap.ywxt.interlocution.utils;

import org.apache.commons.lang3.StringUtils;

public class RedisKeyUtils {

	public final static String split = ":";

	public final static String user = "user";

	public final static String sessionId = "sessionId";

	public final static String mini = "mini";
	public final static String bind = "bind";
	public final static String email = "email";
	public final static String reset = "reset";
	public final static String register = "register";

	/** 用户SessionIdkey **/
	public static String getUserSessionIdKey(Long userId) {
		String key = getRedisKey(user, sessionId, String.valueOf(userId));
		return key;
	}

	public static String getMiniUserKey(String sId) {
		String key = getRedisKey(sessionId, mini, user, sId);
		return key;
	}
	public static String getSessionIdKey(String sId) {
		String key = getRedisKey(sessionId, user, sId);
		return key;
	}
	public static String getBindEmailKey(String mail) {
		String key = getRedisKey(bind, email, mail);
		return key;
	}
	public static String getRegEmailKey(String mail) {
		String key = getRedisKey(register, email, mail);
		return key;
	}
	public static String getResetEmailKey(String mail) {
		String key = getRedisKey(reset, email, mail);
		return key;
	}

	private static String getRedisKey(String... name) {
		StringBuffer sb = new StringBuffer();
		for (String str : name) {
			if (StringUtils.isNotBlank(str)) {
				sb.append(str);
				sb.append(split);

			}
		}
		sb = sb.deleteCharAt(sb.lastIndexOf(split));
		return sb.toString();
	}

}
