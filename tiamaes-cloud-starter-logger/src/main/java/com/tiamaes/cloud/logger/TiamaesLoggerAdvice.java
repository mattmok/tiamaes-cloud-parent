package com.tiamaes.cloud.logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiamaes.security.core.annotation.CurrentUser;
import com.tiamaes.security.core.userdetails.User;


@Aspect
public class TiamaesLoggerAdvice extends AbstractAnnotationAdvice<TiamaesLogger> {
	private static Logger logger = LogManager.getLogger();
	
	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper objectMapper;
	@Autowired
	@Qualifier("kafkaTemplate")
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Value("${tiamaes.logger.enabled:false}")
	private boolean enabled;

	public TiamaesLoggerAdvice() {
		super(TiamaesLogger.class);
	}

	@Pointcut("@annotation(com.tiamaes.logger.TiamaesLogger)")
	public void newLogger() {
	};

	@Around("newLogger()")
	protected Object obtain(final ProceedingJoinPoint pjp) throws Throwable {
		return execute(pjp);
	}

	@Override
	protected Object execute(ProceedingJoinPoint pjp) throws Throwable {
		boolean isDebugEnabled = logger.isDebugEnabled();
		TiamaesLogEntry logEntry = new TiamaesLogEntry();
		logEntry.setId(UUID.randomUUID().toString().replace("-", ""));
		long startTime = System.currentTimeMillis();
		Object[] args = pjp.getArgs();
		final Method method = getMethodToExecute(pjp);
		final String methodName = String.format("%s.%s", method.getDeclaringClass().getName(), method.getName());
		logEntry.setMethodName(methodName);
		
		if(isDebugEnabled){
			logger.debug("methodName: {}", methodName);
		}
		
		final Annotation annotation = method.getAnnotation(annotationClass);
		final Annotation requestMapping = method.getAnnotation(RequestMapping.class);
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Class<?>[] parameterTypes = method.getParameterTypes();
		
		boolean isUpdate = false;
		List<Object> pathVars = new ArrayList<Object>();
		for(int i = 0; i< parameterAnnotations.length; i++){
			Class<?> parameterType = parameterTypes[i];
			for (Annotation parameterAnnotation : parameterAnnotations[i]) {
				if(parameterAnnotation instanceof RequestBody){
					if(isDebugEnabled){
						logger.debug("RequestBody class : {}", args[i].getClass());
						logger.debug("RequestBody : {}", objectMapper.writeValueAsString(args[i]));
					}
				}
				if(parameterAnnotation instanceof PathVariable){
					isUpdate = true;
					pathVars.add(args[i]);
				}
				if(parameterAnnotation instanceof CurrentUser && parameterType.isAssignableFrom(User.class)){
					User user = (User)args[i];
					logEntry.setUser(user);
					
					if(isDebugEnabled){
						logger.debug("User : {}", objectMapper.writeValueAsString(user));
					}
				}
			}
		}
		
		String[] paths = ((String[])_populate(requestMapping, RequestMapping.class, "value"));
		if(paths != null && paths.length > 0){
			Map<String,Object> pathvars = new HashMap<String,Object>();
			String[] pathVarKeys = keysByPathVars(paths[0]);
			for(int i=0;i< pathVarKeys.length;i++){
				pathvars.put(pathVarKeys[i].substring(1, pathVarKeys[i].length() -1), pathVars.get(i));
			}
			if (isDebugEnabled) {
				logger.debug("pathvars: {}", objectMapper.writeValueAsString(pathvars));
			}
		}
		Operation operation = (Operation)_populate(annotation, TiamaesLogger.class, "operation");
		if (operation.equals(Operation.DEFAULT)) {
			RequestMethod[] requestMethods = (RequestMethod[])_populate(requestMapping, RequestMapping.class, "method");
			RequestMethod requestMethod = ((RequestMethod[])requestMethods)[0];
			switch (requestMethod) {
			case PUT:
				operation = isUpdate ? Operation.UPDATE : Operation.ADD;
				break;
			case DELETE:
				operation = Operation.DELETE;
				break;
			default:
				operation = Operation.VISIT;
				break;
			}
		}
		logEntry.setOperation(operation);
		if (isDebugEnabled) {
			logger.debug("operation: {}", operation);
		}
		
		
		try{
			Object result = pjp.proceed();
			logEntry.setStatus(HttpStatus.OK);
			logEntry.setTarget(result);
			if(isDebugEnabled){
				logger.debug("target class: {}", result.getClass());
				logger.debug("target : {}", objectMapper.writeValueAsString(result));
			}
			logEntry.setCast(System.currentTimeMillis() - startTime);
			return result;
		}catch(Exception e){
			logEntry.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			logEntry.setCast(System.currentTimeMillis() - startTime);
			throw e;
		}finally {
			if(enabled){
				kafkaTemplate.send(MessageBuilder.withPayload(logEntry)
						.setHeader(KafkaHeaders.TOPIC, TiamaesLogEntry.class.getName())
						.setHeader(KafkaHeaders.MESSAGE_KEY, operation.name()).build());
				if(logger.isInfoEnabled()){
					logger.info("SEND : {}", objectMapper.writeValueAsString(logEntry));
				}
			}
		}
		
	}
	
	public static String[] keysByPathVars(String path){
		List<String> searchList = new ArrayList<String>();
		Pattern pattern = Pattern.compile("\\{.+?\\}");
		Matcher matcher = pattern.matcher(path);
		while(matcher.find()){
			searchList.add(matcher.group());
		}
		return searchList.toArray(new String[] {});
	}
}
