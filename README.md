# tiamaes-cloud-parent

为了更方便的使用spring cloud 组件，将之前项目中使用的技术的配置部分单独抽取出来，构建出若干的组件。其目的为：
1. 方便业务开发人员集成
2. 规范开发人员的使用
3. 方便架框的统一升级
4. 集成若干边缘服务，如日志、统计、监控等功能

# 1.Overview
##### 1. tiamaes-cloud-core  
将各个子模块中通用的部分归集到这里，包含通用的Object,Utils,Exception等等。  
<pre>*该模块已经包含在各子模块中，不需要单独引入maven依赖*</pre>
##### 2. tiamaes-cloud-dependencies
将各子模块的依赖统一在这里进行管理，包含plugins,dependencyManagement,distributionManagement等
<pre>*该模块只用来进行依赖管理，只需要在 your-project-parent pom.xml中引用即可，不需要单独引入该依赖*</pre>
##### 3. tiamaes-cloud-starter-security
将该模块引入项目中之后，默认开启spring mvc和spring-security支持，提供权限认证支持
##### 5. tiamaes-cloud-starter-swagger2
将该模块引入项目中之后，默认开启springfox-swagger2模块，提供接口文档服务
##### 6. [tiamaes-cloud-starter-zuul](tiamaes-cloud-starter-zuul/README.md)
将该模块引入项目中之后，默认开启netfliex-zuul模块，提供应用网关服务
##### 7. tiamaes-cloud-starter-feign
将该模块引入项目中之后，默认开启feign模块，提供声明式REST客户端服务
##### 8. tiamaes-cloud-starter-mybatis
将该模块引入项目中之后，默认开启mybatis模块，开启mybatis物理分页支持
##### 9. tiamaes-cloud-starter-redis
将该模块引入项目中之后，默认开启redis模块
##### 10. tiamaes-cloud-starter-kafka
将该模块引入项目中之后，默认开启kafka模块
##### 11. tiamaes-cloud-starter-netty
将该模块引入项目中之后，默认开启netty server模块

# 2.Requirements
1. Java 8+
2. Maven 3.2+

# 3.Usage
# 4.More
# 5.Contribute
chenlili@tiamaes.com
# 6.License
Tiamaes cloud project is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
