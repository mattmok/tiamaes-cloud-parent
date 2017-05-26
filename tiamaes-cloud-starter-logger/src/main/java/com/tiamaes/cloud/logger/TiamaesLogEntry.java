package com.tiamaes.cloud.logger;

import java.io.Serializable;
import java.util.Date;

import org.springframework.http.HttpStatus;

import com.tiamaes.security.core.userdetails.User;



public class TiamaesLogEntry implements Serializable {
	private static final long serialVersionUID = -1495293141176143599L;

	private String id;
	private String methodName;
	private Object target;
	private Operation operation;
	private User user;
	private HttpStatus status;
	private Date currentTime;
	private long cast;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public Date getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(Date currentTime) {
		this.currentTime = currentTime;
	}

	public long getCast() {
		return cast;
	}

	public void setCast(long cast) {
		this.cast = cast;
	}
}
