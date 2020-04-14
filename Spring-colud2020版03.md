## Spring Cloud 第三部分 Alibaba

### Springcloud Alibaba

​	官网：github.com/alibaba/spring-cloud-alibaba/blob/master/README-zh.md

​	Api：https://spring-cloud-alibaba-group.github.io/github-pages/greenwich/spring-cloud-alibaba.html

### 服务注册与配置中心Nacos

​	下载：https://github.com/alibaba/nacos/releases/tag/1.1.4

#### nacos的安装

​	官网上下载好后，cmd切换到对应加压目录的bini下执行如下命令

```java
D:\....>nacos-server-1.1.4\nacos\bin>startup.cmd
```

​	执行成功后，访问localhost:8848/nacos，默认的密码以及账户都是nacos

#### 集成案列服务中心

**基于nacos的服务提供者**

​	1、新建module模块cloud-alibaba-payment9001，引入pom依赖配置yml以及编写启动类

```xml
<dependency>
     <groupId>com.alibaba.cloud</groupId>
     <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
...
```

```properties
server.port=8081
spring.application.name=nacos-provider
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
management.endpoints.web.exposure.include=*
```

```java
@SpringBootApplication
@EnableDiscoveryClient
public class NacosProviderMain {
    public static void main(String[] args) {
        SpringApplication.run(NacosProviderMain.class, args);
    }
}
```

​	2、编写业务类

```java
@RestController
public class NacosPaymentController {
    @Value("${server.port}")
    private String serverPort;
    @GetMapping(value = "/payment/nacos/{id}")
    public String echo(@PathVariable("id") Integer id) {
        return "Hello Nacos Discovery " + id + ",Port" + serverPort;
    }
}
```

​	3、测试，http://localhost:9001/payment/nacos/1,查看nacos控制台

**基于nacos的服务消费者**

​	1、新建module模块cloud-alibaba-order83，引入pom依赖配置yml以及编写启动类

​		pom依赖与nacos的服务提供者一致

```yml
server:
  port: 83

spring:
  application:
    name: nacos-order-consumer
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848

# 消费者将要去访问的微服务名称（注册成功的nacos的微服务提供者）
service-url:
  nacos-user-service: http://nacos-payment-provider
```

```java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderNacosMain83 {
    public static void main(String[] args) {
        SpringApplication.run(OrderNacosMain83.class,args);
    }
}
```

2、编写配置类以及业务类

```java
package com.wj.springcloud.config;
@Configuration
public class ApplicationContextConfig {
    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
```

```java
package com.wj.springcloud.controller;
@RestController
public class OrderNacosController {
    @Resource
    private RestTemplate restTemplate;
    @Value("${service-url.nacos-user-service}")
    private String serverUrl;

    @GetMapping(value = "/consumer/payment/nacos/{id}")
    public String paymentInfo(@PathVariable("id") Integer id) {
        String result = restTemplate.getForObject(serverUrl+"/payment/nacos/" + id, String.class);
       return "" + result;
    }
}
```

​	3、测试，启动nacos、payment9001、payment9002、Order83。输入：localhost:83/consumer/payment/nacos/1,可以查看结果

#### 集成案例配置中心

​	1、新建module项目cloud-ali-nacos-client3377，加入pom依赖，配置bootstrap与application的yaml文件以及启动类

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

​	bootstrap.yml

```yml
server:
  port: 3377

spring:
  application:
    name: nacos-config-client
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848 # nacos服务注册中心地址
      config:
        server-addr: localhost:8848 # nacos作为配置中心地址
        file-extension: yaml # 指定yaml格式的配置
# 配置文件读取规则
# ${spring.application.name}-${spring.profile.active}.${spring.cloud.nacos.config.file-extension}
# 结果：nacos-config-client-dev.yaml,这个文件位于nacos控制台中的配置项中
```

​	application.yml

```yml
spring:
  profiles:
    active: dev # 表示开发环境
```

​	启动类

```java
@SpringBootApplication
@EnableDiscoveryClient
public class NacosClientMain {
    public static void main(String[] args) {
        SpringApplication.run(NacosClientMain.class, args);
    }
}
```

​	2、创建业务类

```java
@RestController
@RefreshScope // 刷新scope
public class NacosCenterController {
    @Value("${config.info}")// 参数存在NACOS界面配置中
    private String configInfo;

    @GetMapping("/config/info")
    public String getConfigInfo(){
        return configInfo;
    }
}
```

​	3、在nacos配置化界面中配置nacos-config-client-dev.yaml配置文件，文件后缀与配置文件中必须一致

​	4、测试，启动cloud-ali-nacos-client3377，浏览器中输入：localhost:3377/config/info，即可获得结果

**注**：nacos已经做好了自动刷新不同于config

​	**Nacos作为配置中心-分类配置**

​	1、配置中心配置了不同组且DataID相同，读取配置文件时需要修改yml文件

```yml
server:
  port: 3377

spring:
  application:
    name: nacos-config-client
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848 # nacos服务注册中心地址
      config:
        server-addr: localhost:8848 # nacos作为配置中心地址
        file-extension: yaml # 指定yaml格式的配置
        group: TEST_GROUP # NACOS中自定义组的名称，如果没有设置，默认为DEFAULT_GROUP
```

​	2、配置中心存在多个命名空间，读取配置文件时需要修改yml文件，添加namespace配置，该属性为namespace对应的uuid.

```yml
spring:
  application:
    name: nacos-config-client
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848 # nacos服务注册中心地址
      config:
        server-addr: localhost:8848 # nacos作为配置中心地址
        file-extension: yaml # 指定yaml格式的配置
        group: TEST_GROUP # NACOS中自定义组的名称，如果没有设置，默认为DEFAULT_GROUP
        namespace: 7asdafawe-aw4eq342-34234jk2-2342sa1
```

#### Nacos集群（重要）

​	官网地址：https://nacos.io/zh-cn/docs/cluster-mode-quick-start.html

​	**Nocos的文件修改**

​	1、集群端口号的修改，文件为cluster.conf(注意备份，以防出错)。linux命令如下

```java
cp cluster.conf cluster.conf.bk	// 复制
vim cluster.conf // 编辑
// 添加内容如下
linuxip:端口1	// linuxip是hostname -i能够识别的IP
linuxip:端口2
linuxip:端口3
```

​	2、修改startup.sh文件的修改，传递不同的端口号启动不同的nacos实例，如命令：./startup.sh -p 端口1。实际操作参考doc文件

​	**Nginx的文件修改**

​	1、修改nginx的nginx.conf文件，先备份。实际操作参考doc文件

### 熔断与限流Sentinel

​	介绍：https://github.com/alibaba/Sentinel/wiki/介绍

​	Sentinel 以流量为切入点，从流量控制、熔断降级、系统负载保护等多个维度保护服务的稳定性

​	**丰富的应用场景**：Sentinel 承接了阿里巴巴近 10 年的双十一大促流量的核心场景，例如秒杀（即突发流量控制在系统容量可以承受的范围）、消息削峰填谷、集群流量控制、实时熔断下游不可用应用等。

​	**完备的实时监控**：Sentinel 同时提供实时的监控功能。您可以在控制台中看到接入应用的单台机器秒级数据，甚至 500 台以下规模的集群的汇总运行情况。

​	**广泛的开源生态**：Sentinel 提供开箱即用的与其它开源框架/库的整合模块，例如与 Spring Cloud、Dubbo、gRPC 的整合。您只需要引入相应的依赖并进行简单的配置即可快速地接入 Sentinel。

​	**完善的 SPI 扩展点**：Sentinel 提供简单易用、完善的 SPI 扩展接口。您可以通过实现扩展接口来快速地定制逻辑。例如定制规则管理、适配动态数据源等。

下载地址：https://github.com/alibaba/Sentinel/releases ，[**sentinel-dashboard-1.7.2.jar**](https://github.com/alibaba/Sentinel/releases/download/1.7.2/sentinel-dashboard-1.7.2.jar)

API:https://spring-cloud-alibaba-group.github.io/github-pages/greenwich/spring-cloud-alibaba.html#_spring_cloud_alibaba_sentinel

Sentinal分为2部分

​	核心库（java客户端）不依赖任何框架/库，能够运行与所有java运行时环境，同对于Dubbo/spring cloud等框架也有较好的支持

​	控制台：基于spring boot开发，打包后可以直接运行，不需要额外的Tomcat等应用容器。

#### Sentinal的安装

​	1、下载对应jar包，且端口8080不能被占用，本地环境为JDK8+；

​	2、切换到下载目录，cmd执行如下命令：java -java jar包全称；

​	3、访问Sentinal界面，输入：http://localhost:8080即可，登录名称以及密码为sentinal;

#### 集成案列

​	1、新建model项目cloud-ali-sentinal-service8401，依次引入pom依赖配置yml文件以及启动类

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-nacos</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
 ...
```

```yml
server:
  port: 8401
spring:
  application:
    name: cloud-ali-sentinel-service
  cloud:
    nacos:
      discovery:
        # NACOS服务注册中心地址
        server-addr: localhost:8848
    sentinel:
      transport:
        # 配置 Sentinel dashboard地址
        dashboard: localhost:8080
        # 默认8719端口，假如被占用会自动从8719开始依次+1扫描，直到找到未被占用的端口
        port: 8719
management:
  endpoints:
    web:
      exposure:
        include: '*'
```

```java
@SpringBootApplication
@EnableDiscoveryClient
public class Mainapp8401 {
    public static void main(String[] args) {
        SpringApplication.run(Mainapp8401.class, args);
    }
}
```

​	2、创建业务类

```java
@RestController
public class FlowlLimitController {
    @GetMapping("/testA")
    public String testA(){
       return "--- test a";
    }
    @GetMapping("/testB")
    public String testB(){
        return "--- test b";
    }
}
```

​	3、测试，启动Nacos界面化程序8848,启动Sentinel界面化程序8080，启动微服务cloud-ali-sentinal-service8401.

​	4、执行http://localhost:8401/testA，http://localhost:8401/testB,再登录Sentinel界面即可看到结果。

**Sentinel流控规则**

​	关联：当关联者的访问达到规定值时，当前访问链接挂掉，关联操作设定在当前操作，阀值是针对关联者。

**Sentinal热点规则**

​	对于参数配置，参数索引针对参数的位置，即使单个参数传递的是后台第二个参数也可以通过限制规则。

​	1、兜底单个方法处理，引入SentinelResource注解。

```java
@GetMapping("/testHotKey")
@SentinelResource(value = "testHotKey", blockHandler = "deal_testHotKey")// 兜底处理方法
public String testHotKey(@RequestParam(value = "p1",required = false)String p1,
                         @RequestParam(value = "p2",required = false)String p2){
    return "---- testHotKey";
}

public String deal_testHotKey(String p1, String p2, BlockException e){
    return "---- deal_testHotKey,Is final Method!";
}
```

​	注：@SentinelResource处理的是Sentinel控制台限定规则出的错，对应编码异常不会处理	

```java
@GetMapping("/rateLimit/byUrl")
@SentinelResource(value = "byUrl")// 会调用系统默认自带的兜底方法处理
public CommonResult byUrl(){
    return new CommonResult(200,"byUrl测试ok");
}
```

#### 自定义限流处理逻辑

​	1、定义兜底类以及处理方法

```java
// 自定义全局兜底类
public class CustomerBlockHandler {
    public static CommonResult handlerException(BlockException e){
        return new CommonResult(444,"全局处理方法！");
    }
}
```

​	2、业务类的方法调用异常处理方法

```java
@GetMapping("/rateLimit/customerBlockHandler")
@SentinelResource(value = "customerBlockHandler",
                  blockHandlerClass = CustomerBlockHandler.class,
                  blockHandler = "handlerException")// 会调用CustomerBlockHandler类的handlerException方法处理
public CommonResult customerBlockHandler(){
    return new CommonResult(200,"byUrl测试ok");
}
```

#### 服务熔断

​	1、代码异常处理

```java
@RequestMapping("/consumer/fallback/{id}")
@SentinelResource(value = "fallback",
                  fallback = "deal_runException") // 业务异常处理
public String fallBack(@PathVariable Long id){
    int i = 10/0; // 执行到此处会，会调用deal_runException方法，如果没有配置fallback则返回异常界面
    return "";
}
public String deal_runException(@PathVariable Long id, Throwable e){
   return "deal_runException" + e.getMessage();
}
```

注：同时配置fallBack与blockHandler时，只会进入blockHandler指定的方法。

​	2、异常忽略，配置exceptionsToIgnore属性，当方法中出现该异常时会继续执行下去，忽略异常。

```java
    @RequestMapping("/consumer/fallback/{id}")
    @SentinelResource(value = "fallback",
            fallback = "deal_runException",
            exceptionsToIgnore = {IllegalAccessException.class}) // 业务异常处理
    public String fallBack(@PathVariable Long id){
        int i = 10/0; // 执行到此处会，会调用deal_runException方法，如果没有配置fallback则返回异常界面
        return "";
    }
```

#### Sentinel持久化操作

​	1、修改cloud-ali-sentinal-service8401项目的pom以及yml配置文件

```xml
<!--pom加入如下依赖-->
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-nacos</artifactId>
</dependency>
```

```yml
spring:
  cloud:
    nacos:
	...
    sentinel:
      transport:
		...
        # sentinel持久化配置
        datasourece:
          ds1:
            nacos:
              server-addr: localhost:8848
              dataId: cloudalibaba-sentinel-service
              groupId: DEFAULT_GROUP
              data-type: json
              rule-type: flow
# 激活Sentinel 对 openfeign 的支持    
feign: 
  sentinel: 
    enabled: true
```

​	2、Nacos的控制界面中添加配置，dataId为cloudalibaba-sentinel-service，格式为Json格式，如下：

```json
[
    {
        "resource":"/rateLimit/byUrl",
        "limitApp":"default",
        "grade":1,
        "count":1,
        "strategy":0,
        "controlBehavior":0,
        "clusterMode":false    
    }
]
```

注：resource：资源名称；limitApp：来源应用；grade：阀值类型，0表示线程数，1表示QPS;count:单机阀值；strategy：流控模式，0表示直接，1表示关联，2表示链路；controlBehavior：流控效果，0表示快速失败，1表示Warm Up，2表示排队等候；clusterMode：是否集群。

### 处理分布式事务Seata

​	seata是一款开源的分布式事务解决方案，致力于在微服务框架下提供高性能和简单易用的分布式事务服务。

​	官网：seata.io/zh-cn/

​	seata:唯一ID（XID）与三大组件共同管理，

​		TC - 事务协调者：

​			维护全局和分支事务的状态，驱动全局事务提交或回滚。

​		TM - 事务管理器：

​			定义全局事务的范围：开始全局事务、提交或回滚全局事务。

​		RM - 资源管理器（数据库）

​			管理分支事务处理的资源，与TC交谈以注册分支事务和报告分支事务的状态，并驱动分支事务提交或回滚。

​		原理：事务管理器（TM）向 事务协调者申请开启一个全局事务，全局事务创建成功后生成一个唯一ID,程序运行中各个分支资源管理器（RM）会在 事务协调器（TC ）中注册事务，将注册的事务纳入唯一ID所管辖的事务中。当程序运行的各个分支（某个）事务管理器（TM）向事务协调器（TC ）发起针对唯一ID的全局事务的提交（回滚）时，事务协调器（TC ）完成唯一ID（XID）下的全部事务提交或者回滚。

​	下载：seata-server-0.9.0.zip

#### 	安装

​	1、下载文件后，加压文件

​	2、修改file.conf文件，先备份；

```json
service{
    vgroup_mapping.my_test_tx_group = "fsp_tx_group" // 其他内容不用修改，fsp_tx_group可以自定义命名 
}
store{
    mode = "db"	// 还需修改数据连接配置
}
```

​	3、新建一个seata数据库,执行conf下db_store.sql文件。

​	4、registery.conf文件修改

```
registery{
	type = "nacos"
	nacos{
	serverAddr = "localhost:8848"
	}
}
```

​	5、启动，先启动nacos在启动seata,

​	6、实际生产中，对应应用对应的数据库都需要建日志回滚表,执行表的sql在conf文件夹下。

#### GlobalTransactional的使用

​	1、创建module模块seate-order-service2001，引入依赖配置yml文件以及启动类

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
    <exclusions>
        <exclusion>
            <artifactId>seata-all</artifactId>
            <groupId>io.seata</groupId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <artifactId>seata-all</artifactId>
    <groupId>io.seata</groupId>
    <version>0.9.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```yml
server:
  port: 2001
spring:
  application:
    name: seata-order-service
  cloud:
    alibaba:
      seata: # 自定义事务组的名称需要与seata-server中的对应
        tx-service-group: fsp_tx_group
    nacos:
      discovery:
        server-addr: localhost:8848
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/seata_order
    username: root
    password: 12345

feign:
  hystrix:
    enabled: false
```

```java
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication(exclude = DataSourceProxyConfig.class)// 取消数据源自动创建
public class SeataOrderMainApp2001 {
    public static void main(String[] args) {
        SpringApplication.run(SeataOrderMainApp2001.class, args);
    }
}
```

​	2、创建Service接口以及接口实现

```java
public interface OrderService {
    void create(Order order);
}
// 仓库应用
@FeignClient(value = "seata-storage-service")
public interface StorageService {
    @PostMapping(value = "/storage/decrease") // 操作数据库用POST
    CommonResult decrease(@RequestParam("productId")Long productId,
                          @RequestParam("count") Integer count);
}
// 账户应用
@FeignClient(value = "seata-account-service")
public interface AccountService {
    @PostMapping(value = "/account/decrease") // 操作数据库用POST
    CommonResult decrease(@RequestParam("userId")Long userId,
                          @RequestParam("money") BigDecimal money);
}
```

```java
@Override
@GlobalTransactional(name = "unionname",rollbackFor = Exception.class)// 发生任何异常都回退
public void create(Order order) {
    // 1、订单的创建
    orderDao.create(order);
    // 库存扣减
    storageService.decrease(order.getProductId(), order.getCount());
    // 账户扣减
    accountService.decrease(order.getUserId(),order.getMoney());
    // 2、订单状态修改
    orderDao.update(order.getUserId(), 0);
}
```

​	3、配置类

```java
@Configuration
public class DataSourceProxyConfig {
    @Value("${mybatis.mapperLocations}")
    private String mapperLocations;

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druidDataSource(){
        return new DruidDataSource();
    }

    @Bean
    public DataSourceProxy dataSourceProxy(DataSource dataSource){
        return new DataSourceProxy(dataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactoryBean(DataSourceProxy dataSourceProxy) throws Exception{
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSourceProxy);
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().
                getResources(mapperLocations));
        sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());
        return sqlSessionFactoryBean.getObject();
    }
}

@Configuration
@MapperScan({"com.wj.springcloud"})
public class MybatisConfig {
    //Mybatis扫描实体类配置
}
```

​	4、测试，先启动nacos与seata，再输入localhost:2001/order/create?order.userId = "" &order.productid=""

Seata的TA模式：**方向补偿**原理是将该应用操作的数据还原，利用日志表中的Rollback_info字段里的内容，但是还原之前会进行脏检查。还原数据后删除前置与后置镜像