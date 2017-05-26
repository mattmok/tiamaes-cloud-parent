package com.tiamaes.cloud.security.provisioning;

import java.util.List;

import org.springframework.security.config.annotation.authentication.ProviderManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;

import com.tiamaes.security.core.userdetails.User;


public class InMemoryUserManagerConfigurer<B extends ProviderManagerBuilder<B>>
		extends UserDetailsManagerConfigurer<B, InMemoryUserManagerConfigurer<B>> {

	/**
	 * Creates a new instance
	 */
	public InMemoryUserManagerConfigurer(List<User> users) {
		super(new InMemoryUserManager(users));
	}
}