package com.tiamaes.cloud.swagger;

import static springfox.documentation.schema.ClassSupport.classByName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

public class InMemorySwaggerResourcesProvider implements SwaggerResourcesProvider {
	private boolean swagger1Available;
	private boolean swagger2Available;

	private final DocumentationCache documentationCache;
	
	private String path;

	public InMemorySwaggerResourcesProvider(DocumentationCache documentationCache, Swagger2Properties swagger2Properties) {
		swagger1Available = classByName("springfox.documentation.swagger1.web.Swagger1Controller").isPresent();
		swagger2Available = classByName("springfox.documentation.swagger2.web.Swagger2Controller").isPresent();
		this.documentationCache = documentationCache;
		this.path = swagger2Properties.getPath();
	}

	@Override
	public List<SwaggerResource> get() {
		List<SwaggerResource> resources = new ArrayList<SwaggerResource>();

		for (Map.Entry<String, Documentation> entry : documentationCache.all().entrySet()) {
			String swaggerGroup = entry.getKey();
			if (swagger1Available) {
				SwaggerResource swaggerResource = resource(swaggerGroup, path);
				swaggerResource.setSwaggerVersion("1.2");
				resources.add(swaggerResource);
			}

			if (swagger2Available) {
				SwaggerResource swaggerResource = resource(swaggerGroup, path);
				swaggerResource.setSwaggerVersion("2.0");
				resources.add(swaggerResource);
			}
		}
		Collections.sort(resources);
		return resources;
	}

	private SwaggerResource resource(String swaggerGroup, String baseUrl) {
		SwaggerResource swaggerResource = new SwaggerResource();
		swaggerResource.setName(swaggerGroup);
		swaggerResource.setLocation(swaggerLocation(baseUrl, swaggerGroup));
		return swaggerResource;
	}

	private String swaggerLocation(String swaggerUrl, String swaggerGroup) {
		String base = Optional.of(swaggerUrl).get();
		if (Docket.DEFAULT_GROUP_NAME.equals(swaggerGroup)) {
			return base;
		}
		return "/" + swaggerGroup + base;
	}
}