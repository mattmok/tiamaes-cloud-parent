package com.tiamaes.cloud.swagger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "springfox.documentation.swagger.v2")
public class Swagger2Properties {

	private boolean enabled = false;
	private Info info = new Info();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Info getInfo() {
		return info;
	}

	public void setInfo(Info info) {
		this.info = info;
	}


	public static class Info {
		private String title = "Api Documents";
		private String description;
		private String version;
		
		private Contact contact = new Contact();

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}
		
		public Contact getContact() {
			return contact;
		}

		public void setContact(Contact contact) {
			this.contact = contact;
		}
		
		public static class Contact{
			private String name = "tiamaes";
			private String url;
			private String email;
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public String getUrl() {
				return url;
			}
			public void setUrl(String url) {
				this.url = url;
			}
			public String getEmail() {
				return email;
			}
			public void setEmail(String email) {
				this.email = email;
			}
		}
	}

}
