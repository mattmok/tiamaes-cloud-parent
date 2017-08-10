package com.tiamaes.security.core.userdetails;

public interface UserDetails extends org.springframework.security.core.userdetails.UserDetails {

	String getNickname();
	
	String getMobile();
	
	String getEmail();
}