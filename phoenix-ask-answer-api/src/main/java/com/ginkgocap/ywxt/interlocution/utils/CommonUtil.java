package com.ginkgocap.ywxt.interlocution.utils;


import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;

public class CommonUtil {

	//public static final String JTMOBILE_SERVER_ROOT = ResourceBundle.getBundle("application").getString("jtmobileserver.root");
	static Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	/**
	 * 获取本机地址，格式为：http://www.gingont.com:4445/，或者为:http://192.168.101.41:4445/
	 * @param request
	 * @return
	 */
	/*public static String getServerDomainPath(HttpServletRequest request) {
		if (StringUtils.isNotBlank(JTMOBILE_SERVER_ROOT) && JTMOBILE_SERVER_ROOT.startsWith("http") && JTMOBILE_SERVER_ROOT.endsWith("/"))
			return JTMOBILE_SERVER_ROOT;
		if (null == request)
			throw new RuntimeException("传入参数不正确");
		StringBuilder link = new StringBuilder();
		link.append(request.getScheme()).append("://").append(request.getServerName());
		if ((request.getScheme().equals("http") && request.getServerPort() != 80)
				|| (request.getScheme().equals("https")
				&& request.getServerPort() != 443)) {
			link.append(':');
			link.append(request.getServerPort());
		}
		link.append(request.getContextPath());
		link.append("/");
		return link.toString();
	}*/
	
	/**
	 * web端的JavaScript对长整型的数值无法处理，因此需要转换为字符串
	 */
	public static GsonBuilder createGsonBuilder() {
		GsonBuilder gBuilder = new GsonBuilder().disableHtmlEscaping();
		if (CommonUtil.getRequestIsFromWebFlag() == false) {
			return gBuilder;//不是web端过来的请求，则长整型数值json后不加引号
		}
		gBuilder.registerTypeAdapterFactory(TypeAdapters.newFactory(Number.class, new TypeAdapter<Number>() {
			@Override
			public Number read(JsonReader in) throws IOException {
				JsonToken jsonToken = in.peek();
				switch (jsonToken) {
					case NULL:
						in.nextNull();
						return null;
					case NUMBER:
						return new LazilyParsedNumber(in.nextString());
					default:
						throw new JsonSyntaxException("Expecting number, got: " + jsonToken);
				}
			}

			@Override
			public void write(JsonWriter out, Number value) throws IOException {
				if (value != null && (value instanceof Long || value instanceof BigInteger))
					out.value(value.toString());
				out.value(value);
			}
		}));
		gBuilder.registerTypeAdapterFactory(TypeAdapters.newFactory(BigInteger.class, new TypeAdapter<BigInteger>() {
			@Override
			public BigInteger read(JsonReader in) throws IOException {
				if (in.peek() == JsonToken.NULL) {
					in.nextNull();
					return null;
				}
				try {
					return new BigInteger(in.nextString());
				} catch (NumberFormatException e) {
					throw new JsonSyntaxException(e);
				}
			}

			@Override
			public void write(JsonWriter out, BigInteger value) throws IOException {
				if (null == value)
					out.value(value);
				out.value(value.toString());
			}
		}));
		gBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
		return gBuilder;
	}
	
	/**
	 * web端的JavaScript对长整型的数值无法处理，因此需要转换为字符串
	 */
	public static Gson createGson() {
		Gson gson = createGsonBuilder().create();
		return gson;
	}

	public static long getLongFromJSONObject(JSONObject jsonObject, String key) {
//		String value = jsonObject.getString(key);
//		if (StringUtils.isBlank(value) || StringUtils.isNumeric(value) == false)
//			return 0L;
		return optLongFromJSONObject(jsonObject,key);
	}
	
	public static long optLongFromJSONObject(JSONObject jsonObject, String key) {
		String value = jsonObject.optString(key);
		long result = 0L;
		if (StringUtils.isBlank(value))
			return result;
		try {
			return Long.valueOf(value.trim());
		} catch (Exception e) {
			logger.error("convert string [ " + value.trim() + " ] into long error:" + e);
			return result;
		}
	}

	private static final ThreadLocal<Boolean> REQUEST_IS_FROM_WEB = new ThreadLocal<Boolean>(){
		protected Boolean initialValue() {
			return false;
		}
	};
	public static void setRequestIsFromWebFlag(boolean requestIsFromWeb) {
		REQUEST_IS_FROM_WEB.set(requestIsFromWeb);
	}
	public static boolean getRequestIsFromWebFlag() {
		return REQUEST_IS_FROM_WEB.get();
	}
}
