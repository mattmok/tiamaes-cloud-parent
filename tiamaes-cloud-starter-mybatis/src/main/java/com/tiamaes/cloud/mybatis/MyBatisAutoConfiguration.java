package com.tiamaes.cloud.mybatis;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import com.alibaba.druid.pool.DruidDataSource;
import com.tiamaes.mybatis.Dialect;
import com.tiamaes.mybatis.PageHelper;

@Configuration
@AutoConfigureBefore(org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration.class)
public class MyBatisAutoConfiguration {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(MyBatisAutoConfiguration.class);
	@Autowired
	private MybatisProperties properties;

	@Autowired(required = false)
	private Interceptor[] interceptors;

	@Bean
	@ConfigurationProperties(prefix = "spring.datasource")
	@ConditionalOnClass(com.alibaba.druid.pool.DruidDataSource.class)
	protected DataSource dataSource() {
		return DataSourceBuilder.create().type(DruidDataSource.class).build();
	}

	@Bean
	protected Interceptor pageHelperInterceptor(DataSourceProperties dataSourceProperties) {
		Interceptor interceptor = new PageHelper();
		Properties properties = new Properties();
		String url = dataSourceProperties.getUrl();
		String dialect = Dialect.fromJdbcUrl(url);
		properties.setProperty("dialect", dialect);
		interceptor.setProperties(properties);
		return interceptor;
	}
	
	@Bean
	protected SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setFailFast(true);
		factory.setDataSource(dataSource);
		factory.setVfs(SpringBootVFS.class);
		org.apache.ibatis.session.Configuration configuration = properties.getConfiguration();
		if(configuration == null){
			configuration = new org.apache.ibatis.session.Configuration();
		}
		configuration.setLogImpl(Slf4jImpl.class);
		
		if (!ObjectUtils.isEmpty(this.interceptors)) {
			for(Interceptor interceptor: interceptors){
				configuration.addInterceptor(interceptor);
			}
		}
		factory.setConfiguration(configuration);
		return factory.getObject();
	}
}
