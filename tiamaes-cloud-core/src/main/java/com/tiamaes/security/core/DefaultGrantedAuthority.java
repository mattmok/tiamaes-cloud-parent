package com.tiamaes.security.core;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

public class DefaultGrantedAuthority implements GrantedAuthority {
	private static final long serialVersionUID = -3512280178049212721L;

	private String authority;
	private String alias;

	public DefaultGrantedAuthority() {
	}
	
	public DefaultGrantedAuthority(String authority) {
		this(authority, authority);
	}
	
	public DefaultGrantedAuthority(String authority, String alias) {
		Assert.hasText(authority, "A granted authority textual representation is required");
		this.authority = authority;
		this.alias = alias;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public String getAlias() {
		return alias;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof DefaultGrantedAuthority) {
			return authority.equals(((DefaultGrantedAuthority) obj).authority);
		}

		return false;
	}

	public int hashCode() {
		return this.authority.hashCode();
	}

	public String toString() {
		return this.authority;
	}
}
