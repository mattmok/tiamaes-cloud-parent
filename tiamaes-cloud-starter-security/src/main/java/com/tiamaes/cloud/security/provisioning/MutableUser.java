package com.tiamaes.cloud.security.provisioning;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.UserDetails;

import com.tiamaes.security.core.userdetails.User;



public class MutableUser implements MutableUserDetails {

	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

	private String password;
	private final User delegate;

	public MutableUser(UserDetails user) {
		this.delegate = (User)user;
		this.password = user.getPassword();
	}

	public String getNickname() {
		return delegate.getNickname();
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return delegate.getAuthorities();
	}

	public String getUsername() {
		return delegate.getUsername();
	}

	public boolean isAccountNonExpired() {
		return delegate.isAccountNonExpired();
	}

	public boolean isAccountNonLocked() {
		return delegate.isAccountNonLocked();
	}

	public boolean isCredentialsNonExpired() {
		return delegate.isCredentialsNonExpired();
	}

	public boolean isEnabled() {
		return delegate.isEnabled();
	}

	@Override
	public String getMobile() {
		return delegate.getMobile();
	}

	@Override
	public String getEmail() {
		return delegate.getEmail();
	}
}