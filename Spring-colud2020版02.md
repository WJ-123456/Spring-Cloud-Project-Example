## Spring Cloud 学习笔记第二部分

### 服务配置Config

​	集中式动态管理框架，管理微服务的配置信息。SpringCloud Config为微服务框架中的微服务提供集中化的外部配置支持，配置服务器为各个不同微服务的所有环境提供了一个中心化的外部配置。

​	SpringCloud congfig分为服务端与客户端2部分

​	服务端：

​		分布式配置中心，是个独立的服务应用，用来连接配置服务器并为客户端提供获取配置信息、加密、解密信息的访问接口。

​	客户端：

​		通过指定的配置中心来管理应用资源以及与业务相关的配置内容，并在启动的时候从配置中心获取和加载配置信息，配置服务器默认采用git来存储配置信息，这样有助于对环境配置进行版本管理，并且可以通过git客户端工具来方便的管理与访问配置内容。

#### 集成案例

**服务端**

​	1、在github上新建springcloud-config的仓库，克隆到本地

​	2、新建module项目cloud-config-center3344，导入依赖pom，配置yml以及编写启动类

```xml
<dependency><!--关键依赖-->
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

```yml
server:
  port: 3344
spring:
  application:
    name: cloud-config-center
  cloud:
    config:
      server:
        git:
          uri: git@github.com:WJ-123456/speingcloud-config.git
          search-paths: #搜索目录
            - speingcloud-config
      label: master #读取分支
eureka:
  client:
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/
```

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigMain {
    public static void main(String[] args) {
        SpringApplication.run(ConfigMain.class, args);
    }
}
```

​	3、修改hosts文件，新增127.0.0.1映射至config-3344.com

​	4、测试，前提编辑工具已经配置好了git的相关密码，启动微服务config3344以及Eureka7001，输入http://config-3344.com/master/wenjian.xml即可读取git仓库上的文件。

客户端

​	1、新建module项目cloud-config-client3355，导入依赖pom，配置bootstrap.yml(没有application.yml)以及编写启动类与业务类

```xml
<dependency><!--关键依赖-->
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

```yml
server:
  port: 3355
spring:
  application:
    name: config-client
  cloud:
    config: # Config客户端
      lable: master # 分支名称
      name: config  # 配置文件名称
      profile: dev  # 读取文件后缀名称，上述的3个综合为读取git上config-dev.yml的配置文件
      uri: http://localhost:3344  # 配置中心的地址
eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka
```

```java
@SpringBootApplication
@EnableEurekaClient
public class ConfigClientMain {
    public static void main(String[] args) {
        SpringApplication.run(ConfigClientMain.class, args);
    }
}
```

```java
@RestController
public class ConfigClientController {
    @Value("${config.info}")
    private String configinfo;
    @GetMapping(value = "/configInfo")
    public String getConfigInfo(){
        return configinfo;
    }
}
```

​	2、测试，启动Eureka与config服务端，再启动客户端,输入：http://localhost:3355/configInfo

#### 动态刷新配置

​	对于客户端读取服务端配置信息没有随着git上的修改问题（服务端是修改过来了）。

1、修改客户端client3355,引入springboot的actuator模块

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

2、修改client3355的yml配置文件，添加如下配置

```yml
# 暴露监控端点      
management: 
  endpoint: 
    web: 
      exposure: 
        include: "*"
```

3、在client3355的客户端上添加@RefreshScope的注解

```java
@RestController
@RefreshScope
public class ConfigClientController {
    @Value("${config.info}")
    private String configinfo;
    @GetMapping(value = "/configInfo")
    public String getConfigInfo(){
        return configinfo;
    }
}
```

4、需要外部发送Post请求刷新3355，同步git上的配置信息,避免重启服务。请求方式如下：

```java
curl -X -POST "http://localhost:3355/actuator/refresh"
```

#### 存在问题

​	存在多个客户端需要执行多个手动刷新的脚本，如果特定几个client进行刷新有点麻烦。解决方式可以用**消息总线**控制。

### 消息总线Bus

​	Springcloud Bus配合Springcloud Config使用可以实现配置的动态刷新。

​	Springcloud Bus是用来将分布式系统的节点与轻量级消息系统链接起来的框架，整合了java的事件处理机制和消息中件的功能,支持RabbitMQ与Kafla.能管理和传播分布式系统间的消息，如分布式执行器，可用于广播状态更改、事件推送等，也可以当做微服务的通行通道。

​	总线：

​		使用轻量级的消息代理来构建一个共用的消息主题，并让系统中所有的微服务实例都连接起来，该主题中产生的消息会被所有的实例监听和消费，所以称之为消息总线。在总线上的各个实例都可以方便广播一些需要让其他连接该主题上的实例都知道消息。

​		消息总线的通知方式分为2种，推荐使用第二种方式。

​		第一种：通过通知单个client，再由该client去通知其他的client修改（破坏了client的平衡性）。

​		第二种：通过通知server，所有的client获取server上的信息时能获取到需要修改的信息。

​		ConfigClient上实例都监听MQ中同一个topic(默认是springcloudbus)。当一个服务刷新的时候，会把这个信息放入到topic中，其他监听同一topic的服务就能得到通知，然后去更新自身的配置。

​	**需要安装RabbitMQ**

#### 使用案例

**全局刷新**

​	1、仿照client3355新建client3366,修改配置文件的端口

​	2、消息总线工程center3344需要引入新的依赖，如下

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

​	3、Center3344模块配置文件添加如下配置

```yml
#配置 rabbitmq     
rabbitmq:
  host: localhost
  port: 5672
  username: gust
  password: gust
#... eureka配置（略）
# 暴露bus刷新配置端点
management: 
  endpoints:  # 暴露bus刷新配置的端点
    web:
      exposure:
        include: 'bus-refresh'
```

​	4、client3355与client3366需要修改pom与yml文件以支持总线。

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

​	配置文件在spring的节点下添加rabbitmq的支持

```yml
  rabbitmq:
    host: localhost
    port: 5672
    username: gust
    password: gust
```

​	5、测试，刷新center3344

```
curl -X -POST "http://localhost:3344/actuator/refresh"
```

**定点刷新**

```java
http://localhost:配置中心端口/actuator/bus-refresh/{destination}  //destination:为服务名称加端口号
案例：
curl -X POST "http://localhost:3344/actuator/bus-refresh/config-dlient:3355"
```

### 消息驱动Stream

​	屏蔽底层消息中间件的差异，降低切换成本，统一消息的编程模型。应用程序通过inputs（消费者）或者outputs（生产者）来与Springcloud Stream中的binder对象交互。通过我们配置的binding，而springcloud stream的binder对象负责与消息中间件交互。

​	通过使用spring intergration来连接消息代理中间件实现消息事件的驱动。springcloud Stream为一些供应商的消息中间件产品提供了个性化的自动化配置实现，引用了发布订阅、消费组、分区的三大核心概念。

​	通过定义绑定器作为中间层，完美的实现了应用程序与消息中间件细节之间的隔离。通过向应用程序暴露统一的Channel通道，使得应用程序不需要再考虑各种不同的消息中间件实现。

​	通过定义绑定器Binder作为中间件，实现了应用程序与消息中间件细节之间的隔离。

​	中文api:m.wang1314.com/doc/webapp/topic/20971999.html

#### 集成案例

​	**服务提供**

​	1、新建module项目cloud-stream-provider8801，引入依赖创建yml以及启动类

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
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

```yml
server:
  port: 8801
spring:
  application:
    name: cloud-stream-provider
  cloud:
    stream:
      binders: # 此处配置需要绑定的RabbitMQ的服务信息
        defaultRabbit:  # 定义的名称，用于binding整合
          type: rabbit  # 消息组件类型
          environment:  # 设置rabbitmq的相关环境配置
            spring:
              rabbitmq:
                host: localhost
                port: 5672
                username: gust
                password: gust
      bindings:   # 服务整合处理
        output:   # 通道的名称
          destination: studyExchange    # 使用的Exchange名称定义
          content-type: application/json #  设置消息类型。本次为json,文本则设置“text/plain”
          binder: defaultRabbit # 设置绑定的消息服务的具体设置
eureka:
  client: # 客户端进行Eureka注册配置
    service-url:
      defaultZone: http://localhost:7001/eureka
  instance:
    lease-renewal-interval-in-seconds: 2  # 设置心跳的时间间隔（默认30）
    lease-expiration-duration-in-seconds: 5 # 如果现在超过了5秒间隔（默认90）
    instance-id: send-8801.com  # 在信息列表显示主机名称
    prefer-ip-address: true     # 访问路径变为ip地址
```

```java
@SpringBootApplication
public class StreamProviderMain {
    public static void main(String[] args) {
        SpringApplication.run(StreamProviderMain.class, args);
    }
}
```

​	2、创建业务类以及控制器

```java
public interface IMessageProvider {
    public String send();
}

@EnableBinding(Source.class)    // 定义消息的推送管道
public class MessageProviderImpl implements IMessageProvider {

    @Resource
    private MessageChannel output;  // 消息发送管道

    @Override
    public String send() {
        String ms = UUID.randomUUID().toString();
        output.send(MessageBuilder.withPayload(ms).build());
        return null;
    }
}
```

```java
@RestController
public class SendMessageController {
    @Resource
    private MessageProviderImpl messageProvider;

    @GetMapping(value = "/sendMessage")
    public String senMessage(){
        return messageProvider.send();
    }
}
```

​	3、测试，需要启动Rabbitmq以及Eureka服务，方式：localhost:8801/sendMessage

**消费者**

​	1、创建module项目cloud-stream-consumer8802，cloud-stream-consumer8803，引入依赖以及yml与启动类

​			pom依赖与服务提供项目一致，启动类为springboot标准启动类。yml配置文件如下

```yml
server:
  port: 8802

spring:
  application:
    name: cloud-stream-consumer-8802
  cloud:
    stream:
      binders: # 此处配置需要绑定的RabbitMQ的服务信息
        defaultRabbit:  # 定义的名称，用于binding整合
          type: rabbit  # 消息组件类型
          environment:  # 设置rabbitmq的相关环境配置
            spring:
              rabbitmq:
                host: localhost
                port: 5672
                username: gust
                password: gust
      bindings:   # 服务整合处理
        input:   # 通道的名称
          destination: studyExchange    # 使用的Exchange名称定义
          content-type: application/json #  设置消息类型。本次为json,文本则设置“text/plain”
          binder: defaultRabbit # 设置绑定的消息服务的具体设置

eureka:
  client: # 客户端进行Eureka注册配置
    service-url:
      defaultZone: http://localhost:7001/eureka
  instance:
    lease-renewal-interval-in-seconds: 2  # 设置心跳的时间间隔（默认30）
    lease-expiration-duration-in-seconds: 5 # 如果现在超过了5秒间隔（默认90）
    instance-id: receive-8802.com  # 在信息列表显示主机名称
    prefer-ip-address: true     # 访问路径变为ip地址
```

​	2、创建业务类

```java
@Component
@EnableBinding(Sink.class)
public class ReceiveMessageController {
    @Value("${server.port}")
    private String serverPort;

    @StreamListener(Sink.INPUT)
    public void input(Message<String> message){
        System.out.println("consumer one,--->>accept message:" + message.getPayload() + "; port :" + serverPort);
    }
}
```

​	3、测试，启动RibbitMQ与Eureka以及8801与8802，栏目输入：localhost:8801/sendMessage，可以看到8802的打印语句。

#### 分组消费与持久化

​	**重复消费**

​		对于消息重复消费问题（比如一个订单被2个开发商接到），可以利用Stream中的消息分组来解决。原因是默认分组group是不同的，组的流水号不一样被认为不同组可以消费。

​			原理：利用微服务应用放在同一个group中，就能够保证消息只会被其中一个应用消费一次，不同的组是可以消费的，同一个组内会发生竞争关系，只有其中一个可以消费。

​	**自定义分组**

​	1、修改yml文件

```yml
spring:
  application:
    name: cloud-stream-consumer-8802
  cloud:
    stream:
      binders: # 此处配置需要绑定的RabbitMQ的服务信息
        defaultRabbit:  # 定义的名称，用于binding整合
		...
      bindings:   # 服务整合处理
        input:   # 通道的名称
          destination: studyExchange    # 使用的Exchange名称定义
		  ...
          group: groupa # 自定义分组
```

​	**轮询消费**

​	1、修改consumer8802与consumer8803项目的yml配置文件，将group设置为同一个，那么每次消息消费只能被8802与8803其中的某一个接收到。

​	**持久化**

​	为项目yml文件添加group配置后，stream就能够进行持久化。持久化作用：消费者在停机状态下，期间生产者发送的消息在消费者重启后能够接收到，如果没有配置持久化，消费者重启后将不会接收消息。

### 请求链路跟踪Sleuth

​	Springcloud Sleuth提供了一套完整的服务跟踪的解决方案，兼容支持**zipkin**.

#### zipkin的下载安装

​	下载：https://dl.bintray.com/openzipkin/maven/io/zipkin/java/zipkin-server/

​	将下载好的jar文件，用cnd切换到下载目录，执行以下命令

```java
java -jar zipkin-server-2.12.9-exec.jar
```

​	**cmd快速切换到指定文件夹下，在所在文件窗口的URL上直接输入cmd即可**。

​	访问控制台：localhost:9411/zipkin/

#### zipkin集成案列

​	**服务提供**

​	1、引入pom依赖，在payment8085与order项目的基础上加入如下依赖。

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

​	2、在payment8085与order项目的yml配置文件,在spring.application同一节点下添加zipkin与sleuth节点，结果如下：

```yml
spring:
  application:
    name: cloud-payment-service
  zipkin:
    base-url: http://localhost:9411
  sleuth:
    sampler:  # 采样率值介于0到1之间，1表示全部采集
      probability: 1
```

​	3、payment8085业务类上添加如下方法

```java
@GetMapping(value = "/payment/zipkin")
public String paymentZipkin(){
    System.out.println("zip 测试");
    return "zipkin 测试！";
}
```

​	4、order的业务类上添加如下方法

```java
@GetMapping("/consumer/payment.zipkin")
public String paymentZipkin(){
    String result = restTemplate.getForObject("http://localhost:8085" + "/payment/zipkin", String.class);
    return result;
}
```

​	5、测试，启动order与payment，输入order的请求localhost/consumer/payment.zipkin进行测试，再http://localhost:9411查看结果











