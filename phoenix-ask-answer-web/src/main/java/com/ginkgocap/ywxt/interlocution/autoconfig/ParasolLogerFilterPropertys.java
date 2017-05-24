package com.ginkgocap.ywxt.interlocution.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "parasol.logger")
public class ParasolLogerFilterPropertys {
	private String parameter;
	private String urlPatterns = "/*";
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getUrlPatterns() {
		return urlPatterns;
	}

	public void setUrlPatterns(String urlPatterns) {
		this.urlPatterns = urlPatterns;
	}
	
	
	
}
