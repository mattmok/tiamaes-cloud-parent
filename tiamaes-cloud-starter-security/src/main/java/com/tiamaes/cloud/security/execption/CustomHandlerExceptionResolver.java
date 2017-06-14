package com.tiamaes.cloud.security.execption;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiamaes.cloud.exception.IllegalAccessException;
import com.tiamaes.cloud.exception.IllegalArgumentException;
import com.tiamaes.cloud.exception.IllegalStateException;

public class CustomHandlerExceptionResolver implements HandlerExceptionResolver {
	private static Logger logger = LogManager.getLogger(CustomHandlerExceptionResolver.class);

	private ObjectMapper jacksonObjectMapper;

	public CustomHandlerExceptionResolver(ObjectMapper jacksonObjectMapper) {
		this.jacksonObjectMapper = jacksonObjectMapper;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
		ModelAndView view = new ModelAndView();
		int code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		String message = exception.getMessage();
		if (exception instanceof IllegalArgumentException || exception instanceof IllegalStateException) {
			code = HttpServletResponse.SC_BAD_REQUEST;
			message = exception.getMessage();
		} else if (exception instanceof IllegalAccessException || exception instanceof AccessDeniedException) {
			code = HttpServletResponse.SC_UNAUTHORIZED;
			message = exception.getMessage();
		} else {
			view = null;
			logger.error(exception.getMessage(), exception);
			message = "服务异常，请稍后重试...";
		}

		java.io.PrintWriter writer = null;
		try {
			response.setStatus(code);
			response.setHeader("Expires", "0");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Content-Type", "application/json;charset=UTF-8");
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			writer = response.getWriter();
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("state", false);
			result.put("code", code);
			result.put("message", message);
			jacksonObjectMapper.writeValue(writer, result);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
		return view;
	}
}
