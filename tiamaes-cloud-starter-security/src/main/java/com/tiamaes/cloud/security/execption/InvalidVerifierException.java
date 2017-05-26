package com.tiamaes.cloud.security.execption;

import org.springframework.security.core.AuthenticationException;

public class InvalidVerifierException extends AuthenticationException {
	private static final long serialVersionUID = -2611957529782343318L;
	
	public InvalidVerifierException(String msg) {
		super(msg);
	}
	
	public InvalidVerifierException(String msg, Throwable t) {
        super(msg, t);
    }
}
