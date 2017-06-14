package com.tiamaes.cloud.zuul.filter;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class HttpSessionZuulPreFilter extends ZuulFilter {
	private static final Logger logger = LoggerFactory.getLogger(HttpSessionZuulPreFilter.class);
	
	private RedisOperationsSessionRepository repository;
	public HttpSessionZuulPreFilter(RedisOperationsSessionRepository repository) {
		this.repository = repository;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
		RequestContext context = RequestContext.getCurrentContext();

		HttpSession httpSession = context.getRequest().getSession();
		Session session = repository.getSession(httpSession.getId());
		context.addZuulRequestHeader("Cookie", "SESSION=" + httpSession.getId());
		if (logger.isDebugEnabled()) {
			logger.debug("ZuulPreFilter session proxy: {}", session.getId());
		}
		return null;
	}

	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return 0;
	}

}
