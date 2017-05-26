package com.tiamaes.cloud.security.provisioning;

import com.tiamaes.security.core.userdetails.UserDetails;

public interface MutableUserDetails extends UserDetails {

	void setPassword(String password);

}