package com.ginkgocap.ywxt.interlocution.autoconfig;

import com.ginkgocap.ywxt.interlocution.web.filter.ParasolLoggerFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(ParasolLogerFilterPropertys.class)
@ConditionalOnClass(ParasolLoggerFilter.class)
@ConditionalOnProperty(prefix = "parasol.logger", value = "enabled", matchIfMissing = true)
public class ParasolLogerFilterAutoConfig {

	@Autowired
	private ParasolLogerFilterPropertys exampleFilterPropertys;

	@Bean(name = "parasolLogfilter")
	@ConditionalOnMissingBean(ParasolLoggerFilter.class)
	public ParasolLoggerFilter filter() {
		ParasolLoggerFilter filter = new ParasolLoggerFilter();
		filter.setParameter(exampleFilterPropertys.getParameter());
		return filter;
	}

	@Bean
	public FilterRegistrationBean filterRegistrationBean(ParasolLoggerFilter parasolLoggerFilter) {
		FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
		filterRegistrationBean.setFilter(parasolLoggerFilter);
		filterRegistrationBean.setEnabled(true);
		filterRegistrationBean.addUrlPatterns(exampleFilterPropertys.getUrlPatterns());
		return filterRegistrationBean;
	}
}
