package com.tiamaes.cloud.security.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.memory.UserAttribute;
import org.springframework.security.core.userdetails.memory.UserAttributeEditor;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.Assert;

import com.tiamaes.security.core.DefaultGrantedAuthority;
import com.tiamaes.security.core.userdetails.User;


public class InMemoryUserManager implements UserDetailsManager {
	protected final Log logger = LogFactory.getLog(getClass());

	private final Map<String, MutableUserDetails> users = new HashMap<String, MutableUserDetails>();

	private AuthenticationManager authenticationManager;

	public InMemoryUserManager(Collection<User> users) {
		for (User user : users) {
			createUser(user);
		}
	}

	public InMemoryUserManager(Properties users) {
		Enumeration<?> names = users.propertyNames();
		UserAttributeEditor editor = new UserAttributeEditor();

		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			editor.setAsText(users.getProperty(name));
			UserAttribute attr = (UserAttribute) editor.getValue();
			
			List<DefaultGrantedAuthority> authorities = new ArrayList<DefaultGrantedAuthority>();
			List<GrantedAuthority> list = attr.getAuthorities();
			for(GrantedAuthority authority : list){
				DefaultGrantedAuthority grantedAuthority = new DefaultGrantedAuthority(authority.toString(), authority.toString());
				authorities.add(grantedAuthority);
			}
			
			User user = new User(name, attr.getPassword(), authorities);
			createUser(user);
		}
	}

	public void createUser(UserDetails user) {
		Assert.isTrue(!userExists(user.getUsername()), "username not exists");

		users.put(user.getUsername().toLowerCase(), new MutableUser(user));
	}

	public void deleteUser(String username) {
		users.remove(username.toLowerCase());
	}

	public void updateUser(UserDetails user) {
		Assert.isTrue(userExists(user.getUsername()), "username not exists");

		users.put(user.getUsername().toLowerCase(), new MutableUser(user));
	}

	public boolean userExists(String username) {
		return users.containsKey(username.toLowerCase());
	}

	public void changePassword(String oldPassword, String newPassword) {
		Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();

		if (currentUser == null) {
			// This would indicate bad coding somewhere
			throw new AccessDeniedException(
					"Can't change password as no Authentication object found in context " + "for current user.");
		}

		String username = currentUser.getName();

		logger.debug("Changing password for user '" + username + "'");

		// If an authentication manager has been set, re-authenticate the user
		// with the
		// supplied password.
		if (authenticationManager != null) {
			logger.debug("Reauthenticating user '" + username + "' for password change request.");

			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, oldPassword));
		} else {
			logger.debug("No authentication manager set. Password won't be re-checked.");
		}

		MutableUserDetails user = users.get(username);

		if (user == null) {
			throw new IllegalStateException("Current user doesn't exist in database.");
		}
		user.setPassword(newPassword);
	}

	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		com.tiamaes.security.core.userdetails.UserDetails user = users.get(username.toLowerCase());

		if (user == null) {
			throw new UsernameNotFoundException(username);
		}
		
		List<DefaultGrantedAuthority> authorities = new ArrayList<DefaultGrantedAuthority>();
		Collection<? extends GrantedAuthority> list = user.getAuthorities();
		for(GrantedAuthority authority : list){
			DefaultGrantedAuthority grantedAuthority = new DefaultGrantedAuthority(authority.toString(), authority.toString());
			authorities.add(grantedAuthority);
		}
		return new User(user.getUsername(), user.getPassword(), user.getNickname(), user.isEnabled(),
				user.isAccountNonExpired(), user.isCredentialsNonExpired(), user.isAccountNonLocked(), authorities);
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
}
