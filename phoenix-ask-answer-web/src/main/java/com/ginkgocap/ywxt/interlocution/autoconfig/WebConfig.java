package com.ginkgocap.ywxt.interlocution.autoconfig;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ginkgocap.ywxt.interlocution.web.filter.AppFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.Filter;
import java.util.List;

@Configuration
public class WebConfig {
	@Bean
	public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		objectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter(objectMapper);
		return jsonConverter;
	}

	@Bean
	public ShallowEtagHeaderFilter shallowETagHeaderFilter() {
		return new ShallowEtagHeaderFilter();
	}

	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
	    factory.addServerCustomizers(new JettyServerCustomizer(){
			@Override
			public void customize(Server server) {
				QueuedThreadPool queuedThreadPool = new QueuedThreadPool(1024);
				queuedThreadPool.setMaxQueued(1024);
				queuedThreadPool.setMinThreads(50);
				server.setThreadPool(queuedThreadPool);
				
			}});
	    //factory.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/notfound.html"));
	    return factory;
	}	
	
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(customJackson2HttpMessageConverter());
		// super.addDefaultHttpMessageConverters(converters);
	}

	@Bean
	public FilterRegistrationBean appFilterRegistration() {

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(appFilter());
		registration.addUrlPatterns("/*");
		registration.addInitParameter("excludedUrl", "/paramValue");
		registration.setName("appFilter");
		return registration;
	}

	@Bean(name = "appFilter")
	public Filter appFilter() {
		return new AppFilter();
	}
}
