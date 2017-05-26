package com.tiamaes.cloud.swagger;

import static springfox.documentation.schema.AlternateTypeRules.newRule;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.classmate.TypeResolver;
import com.tiamaes.cloud.swagger.Swagger2Properties.Info;
import com.tiamaes.security.core.userdetails.User;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Documentation;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2AutoConfiguration {
	@Value("${server.contextPath:/}")
	private String contextPath;
	
	@Autowired
	private TypeResolver typeResolver;
	@Autowired(required = false)
	private ZuulProperties zuulProperties;
	@Autowired
	private Swagger2Properties swagger2Properties;
	
	@Bean
	public Docket defaultDocket() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
				.paths(PathSelectors.any())
				.build()
				.ignoredParameterTypes(Principal.class, Authentication.class, User.class)
				.pathMapping(contextPath)
				.directModelSubstitute(LocalDate.class, String.class).genericModelSubstitutes(ResponseEntity.class)
				.alternateTypeRules(
					newRule(typeResolver.resolve(DeferredResult.class,
							typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
							typeResolver.resolve(WildcardType.class)))
				.useDefaultResponseMessages(true)
//				.globalResponseMessage(RequestMethod.GET,
//					newArrayList(
//						new ResponseMessageBuilder().code(500).message("500 message").responseModel(new ModelRef("Error"))
//						.build()))
//				.securitySchemes(newArrayList(apiKey()))
//				.securityContexts(newArrayList(securityContext()))
//				.globalOperationParameters(
//					newArrayList(new ParameterBuilder()
//						.name("someGlobalParameter")
//						.description("Description of someGlobalParameter")
//						.modelRef(new ModelRef("string"))
//						.parameterType("query")
//						.required(true)
//						.build()));
				.enableUrlTemplating(true)
				.enable(swagger2Properties.isEnabled() || zuulProperties == null);
	}

	private ApiInfo apiInfo() {
		Info info = swagger2Properties.getInfo();
		return new ApiInfoBuilder()
			.title(info.getTitle())
			.description(info.getDescription())
			.contact(new Contact(info.getContact().getName(), info.getContact().getUrl(), info.getContact().getEmail()))
			.version(info.getVersion())
			.build();
	}
		
//	@Bean
//	protected UiConfiguration uiConfig() {
//		return new UiConfiguration(
//			"validatorUrl", 	// url
//			"none", 			// docExpansion 			=> none | list
//			"alpha", 			// apiSorter 				=> alpha
//			"schema", 			// defaultModelRendering 	=> schema
//			UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS, 
//			false, 				// enableJsonEditor 		=> true | false
//			true, 				// showRequestHeaders 		=> true | false
//			60000L); 			// requestTimeout => in milliseconds, defaults to null (uses jquery xh timeout)
//	}
		
		
	@Bean
	public Swagger2Properties swagger2Properties() {
		return new Swagger2Properties();
	}
	
	@Bean
	@Primary
	public SwaggerResourcesProvider swaggerResources(DocumentationCache documentationCache){
		if(zuulProperties != null && zuulProperties.getRoutes() != null){
			zuulProperties.getRoutes().forEach((name, zuulRoute) -> {
				Documentation documentation = new Documentation(name, null, null, null, null, null, null, null, null);
				documentationCache.addDocumentation(documentation);
			});
		}
		return new InMemorySwaggerResourcesProvider(documentationCache);
	}
}
