package com.tiamaes.cloud.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public class SecurityProperties extends org.springframework.boot.autoconfigure.security.SecurityProperties {

	private Form form = new Form();

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public static class Form {
		private boolean enabled = false;

		private String loginPage = "/login.html";

		private String loginProcessingUrl = "/login";

		private String defaultSuccessUrl = "/index.html";

		private Parameters parameters = new Parameters();

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getLoginPage() {
			return loginPage;
		}

		public void setLoginPage(String loginPage) {
			this.loginPage = loginPage;
		}

		public String getLoginProcessingUrl() {
			return loginProcessingUrl;
		}

		public void setLoginProcessingUrl(String loginProcessingUrl) {
			this.loginProcessingUrl = loginProcessingUrl;
		}

		public Parameters getParameters() {
			return parameters;
		}

		public void setParameters(Parameters parameters) {
			this.parameters = parameters;
		}

		public String getDefaultSuccessUrl() {
			return defaultSuccessUrl;
		}

		public void setDefaultSuccessUrl(String defaultSuccessUrl) {
			this.defaultSuccessUrl = defaultSuccessUrl;
		}

		public static class Parameters {
			private String username = "username";
			private String password = "password";

			public String getUsername() {
				return username;
			}

			public void setUsername(String username) {
				this.username = username;
			}

			public String getPassword() {
				return password;
			}

			public void setPassword(String password) {
				this.password = password;
			}
		}
	}
}
