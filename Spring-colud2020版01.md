## Spring-Cloud(不在能知,乃在能行）

版本选择：**Springboot 2.X版、SpringCloud H版、SpringCloud Alibaba**

SpringBoot源码地址：https://github.com/spring-projects/spring-boot/releases

SpringBoot2新特性:http://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Release-Nokes

Spring-Cloud源码:https://github.com/spring-projects/spring-cloud/wiki

spring-boot与spring-clould版本之间的选择：https://spring.io/projects/spring-cloud#overview

具体选择：https://start.spring.io/actuator/info

### Euraka

**1、什么是服务治理？**

​	Springcloud封装了Netflix公司开发的Eureka模块来实现服务治理，

​	在传统的rpc远程调用框架中，管理每个服务于服务之间依赖关系比较复杂，管理比较复杂，所以需要使用服务治理，管理服务于服务之间的依赖关系，可以实现服务调用，负载均衡，容错等，实现服务的发现与注册。

**2、什么是服务的注册与发现**

​	Eureka采用了CS的设计架构，Erueka Server作为服务注册功能的服务器，他是服务中心。而系统中的其他服务，使用Eureka的客户端连接到Erueka Server并维持心跳连接。这样系统的维护人员就可以通过Erueka server来监控系统中各个微服务是否正常运行。

​	在服务注册与发现中，有个注册中心。当服务启动的时候，会把当前自己的服务器信息 如：服务器的通讯地址等以别名的方式注册到注册中心上。另一方（消费者|服务提供者），以该别名的方式到注册中心上获取实际的服务通讯地址，而后再实现本地的RPC调用RPC远程调用框架核心设计思想；在注册中心中，因为使用注册中心管理每个服务于服务之间的一个依赖关系（服务治理概念）。在任何rpc远程调用框架中，都会有一个服务注册中心（存放服务器地址相关信息（接口地址））

#### 服务组件

​	**Eureka Server**

​	提供服务注册的服务，各个微服务节点通过配置启动后，会在EurekaServer中进行注册，这样EruekaServer中的服务注册表会将存储所有可用的服务节点的信息，服务节点的信息可以在界面中直观看到。

​	**Eureka Client**

​	通过注册中心进行访问EruekaServer服务，是个java客户端，用于简化EruekaServer的交互，客户端同时也具备一个内置、使用轮询（round-robin）负载算法的负载均衡器。在应用启动后，将会向EurekaServer发送心跳（默认**周期为30秒**）。如果EruakeServer在多个心跳周期内没有接收到某个节点的心跳，EruekaServer将会从服务注册表中把这个服务节点移除（**默认90秒**）。

#### 集成案列

​	**服务端集成**

​	pom文件(关键部分)，实际可参考D:\workspace\IDEASPACE\Spring-Cloud-Project下的cloud-erueka-server的pom文件。

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

application.yml配置信息

```yml
server:
  port: 7001
eureka:
  instance:
    hostname: localhost
  client:
    # false 表示不向注册中心注册自己
    register-with-eureka: false
    # false表示自己端就是服务注册中心。职责就是维护服务实例，并不需要去检索服务
    fetch-registry: false
    service-url:
      # 设置与Eureka Server交互的地址查询服务于注册服务都需要依赖这个地址
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

启动类需要加上@EnableEurekaServer，代码如下：

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerMain {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerMain.class, args);
    }
}
```

访问地址：http://localhost:7001，可以看到访问结果。页面包含Spring Eureka标志。

​	**客户端集成**

​	pom文件(关键部分)，实际可参考D:\workspace\IDEASPACE\Spring-Cloud-Project下的cloud-provider-payment的pom文件。

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

application.yml新增内容

```yml
eureka:
  client:
    # 表示是否将自己注册进EurekaServer，默认为true
    register-with-eureka: true
    # 是否从EurekaServer中抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true,这样才能配合ribbon使用负载均衡
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:7001/eureka/
```

启动类，需要添加@EnableEurekaClient注解，具体代码如下：

```java
@SpringBootApplication
@EnableEurekaClient
public class PaymentMain {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain.class,args);
    }
}
```

注：需要先启动EurekaServer后，再启动EurekaClient服务。

#### Eureka集群

​	微服务RPC远程服务调用最核心的是高可用，解决方案是搭建Eureka注册中心集群，实现负载均衡与故障容错。实现互相注册，相互守望。

​	1、修改hosts文件,追加如下映射。

```javascript
127.0.0.1 eureka7001.com
127.0.0.1 eureka7002.com
```

​	2.修改Eureka服务项目的yml配置文件，修改内容如下

​	7001服务器yml文件

```yml
server:
  port: 7001
eureka:
  instance:
    hostname: eureka7001.com
  client:
    # false 表示不向注册中心注册自己
    register-with-eureka: false
    # false表示自己端就是服务注册中心。职责就是维护服务实例，并不需要去检索服务
    fetch-registry: false
    service-url:
      # 设置与Eureka Server交互的地址查询服务于注册服务都需要依赖这个地址
      defaultZone: http://eureka7002.com:7002/eureka/
```

​	7002服务器yml文件

```yml
server:
  port: 7002
eureka:
  instance:
    hostname: eureka7002.com #Eureka服务端的真实名称
  client:
    # false 表示不向注册中心注册自己
    register-with-eureka: false
    # false表示自己端就是服务注册中心。职责就是维护服务实例，并不需要去检索服务
    fetch-registry: false
    service-url:
      # 设置与Eureka Server交互的地址查询服务于注册服务都需要依赖这个地址
      defaultZone: http://eureka7001.com:7001/eureka/
      # 如果不想集群，可以将7001改为7002
```

其他服务注册金Eureka服务，yml文件关键配置

```yml
eureka:
  client:
    # 表示是否将自己注册进EurekaServer，默认为true
    register-with-eureka: true
    # 是否从EurekaServer中抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true,这样才能配合ribbon使用负载均衡
    fetch-registry: true
    service-url:
      #defaultZone: http://localhost:7001/eureka/ #单机版
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/ #集群版
```

注：如果多个Eureka服务集群，集群配置中defaultZone为其他Eureka服务访问地址

**应用服务集群**

​	不是EUREKA服务，如：cloud-provider-payment模块，可以参照具体代码，复制一份应用服务模块项目，将yml文件端口改为8002，其余配置与应用服务配置一样（存在2个应用服务，端口不一样，分别为8085与8082）。

​	1、修改订单模块的controller部分代码，将固定ip地址改为Eureka服务中**Application**应用的名称，如下：

```
public static final String PAYMENT_URL = "http://CLOUD-PAYMENT-SERVICE";
```

​	2、修改订单模块的spring配置java类ApplicationContextConfig类，加入@LoadBalanced注解，使RestTemplate类具有负载均衡的能力。

**Eureka服务信息界面**

​	1、服务名称修改，修改Eureka服务实例的id名称，在Eureka服务项目的yml文件中Eureka服务配置中加上eureka.instance.instance-id=payment8085，如下配置

```yml
eureka:
	...
    instance: 
      instance-id: payment8085
```

​	2、健康检查，在浏览器上输入http://eureka7001.com:7001/eureka/health可检查Eureka的服务状态。

​	3、访问信息有ip信息显示

```yml
eureka:
  ...
  instance:# Client级别
    instance-id: payment8085 
    prefer-ip-address: true # 鼠标移到服务名称上去时，左下角显示ip地址
```

​	注：需要加入spring-boot的这些依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

#### 服务发现Discovery

​	对于注册进Eureka里面的微服务，可以通过服务发现来获取注册过的服务信息。

​	1、在服务应用的controller代码中，加入DiscoveryClient类将服务应用注册在Eureka的信息暴露给其他服务应用。代码如下：

```java
@Resource
private DiscoveryClient discoveryClient;

// 将该服务在Eureka服务注册的信息暴露给Eureka的客户客户端
@GetMapping(value = "/payment/discovery")
public Object getEurekaServerInfo(){
    // 获取服务名称
    List<String> services = discoveryClient.getServices();
    // 获取服务实例信息，包含服务id,服务器名称，端口，访问uri
    List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
    return this.discoveryClient;
}
```

​	2、在服务应用启动类加入@EnableDiscoveryClient注解，如下：

```java
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
public class PaymentMain {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain.class,args);
    }
}
```

#### Eureka的自我保护

​	**什么是自我保护？**

​	某时刻某个微服务不可用了，Eureka不会立即清理，依旧会对该微服务的信息进行保存。原因是Eureka是分布式（CAP）中的AP分支。保证Eureka服务的稳定性以及健壮性。

​	**禁用掉Euraka的自我保护**

​	1、在Eureka的服务(cloud-eureka-server)中加入如下配置

```yml
eureka:
	...
  server:# Client级别
    # 禁用自我保护机制，保证不可用微服务信息被及时删除
    enable-self-preservation: false
    # 时间间隔检查
    eviction-interval-timer-in-ms: 2000
```

​	2、在Eureka的注册的微服务（cloud-provider-payment）的yml配置文件中，配置如下代码：

```yml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka/ #单机版
  instance:
    instance-id: payment8085
    prefer-ip-address: true # 鼠标移到服务名称上去时，左下角显示ip地址
    # 注册在Eureka服务中的微服务端向注册中心发送心跳的时间间隔
    lease-renewal-interval-in-seconds: 1
    # Eureka 注册中心在收到最后一次心跳后等待的时间上限，超过配置将会被踢出服务
    lease-expiration-duration-in-seconds: 2
```

​	注：defaultZone配置属性需要改成单机版

Eureka官网地址：github.com/Netflix/rureka/wiki

### zookeeper

安装在Linux上zookeeper,版本：zookeeper-3.4.9

Linux关闭防火墙：systemctl stop firewalld，防火墙状态检查：systemctl status firewalld

Linux启动zookeeper服务：进入zookeeper的bin目录，执行 ./zkServer.sh start

Linux连接zookeeper服务：进入zookeeper的bin目录，执行 ./zkCli.sh

​	**包版本冲突**

问题：zookeeper的jar包版本冲突，解决方式排出对应包。pom文件如下：

```xml
<!--springboot整合zookeeper客户端-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
    <exclusions>
        <exclusion>先排出本身自带的jar包
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>引入对应的jar包
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
    <version>3.4.9</version>
</dependency>
```

服务节点：分为临时节点、带序号的临时节点，持久节点，带序号的持久节点。zookeeper为临时节点

#### 集成案列

​	**服务提供者集成**

​	pom文件，加入如下关键依赖

```xml
<!--springboot整合zookeeper客户端-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.apache.zookeeper</groupId>
    <artifactId>zookeeper</artifactId>
    <version>3.4.9</version>
</dependency>	
```

​	关键代码可以参照：cloud-provider-payment-z-8004项目

​	yml配置文件

```yml
server:
  port: 8004

spring:
  application:
    name: cloud-payment-service
  cloud:
    zookeeper:
      connect-string: 192.168.1.104:2131 #linux上zookeeper服务地址以及端口
```

​	启动类

```java
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentZooMain {
    public static void main(String[] args) {
        SpringApplication.run(PaymentZooMain.class, args);
    }
}
```

​	controller控制类

```java
@RestController
public class PaymentController {
    @Value("${server.port}")
    private String serverPort;

    @RequestMapping(value = "/payment/zk")
    public String getPaymentZooInfo(){
        return "SpringCloud With Zookeeper Port:" + serverPort;
    }
}
```

​	测试地址：localhost:8004/payment/zk

​	**服务消费者集成**

​	配置文件与服务提供者集成的pom文件的依赖一致。

​	yml配置文件信息

```yml
server:
  port: 80

spring:
  application:
    name: cloud-consumer-order
  cloud:
    zookeeper:
      connect-string: 192.168.1.104:2131 # 集群也是加逗号再加地址
```

​	启动类信息

```java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderZooMain {
    public static void main(String[] args) {
        SpringApplication.run(OrderZooMain.class, args);
    }
}
```

配置类

```java
@Configuration
public class ApplicationContextBean {
    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
```

controller控制类

```java
@RestController
public class OrderZooController {
    public static final String INVOKE_URL = "http://cloud-payment-service";

    @Resource
    private RestTemplate restTemplate;

    @GetMapping(value = "/consumer/payment/zk")
    public String getPaymentInfo(){
        String result = restTemplate.getForObject(INVOKE_URL+"/payment/zk",String.class);
        return "" + result;
    }
}
```

测试:localhost/consumer/payment/zk

### Consul

官网地址：consul.io/intro/index.html(go语言)。

中文地址：springcloud.cc/spring-cloud-consul.html

​	Consul是一套开源的分布式服务发现与配置管理系统，由HashiCrop公司用go语言开发。提供了微服务系统中的服务治理、配置中心、控制总线等功能。这些功能中的每一个都可以根据需要单独使用，也可以使用构建全方位的服务网格，总之Consul提供了一种完整的网络网格解决方案。

​	它具有很多的有点。包括：基于raft协议、比较简洁；支持健康检查，同时支持http与dns协议，支持跨数据中心的wan集群，提供了图形界面 夸平台，支持linux,mac,windows.

**安装以及启动**

​	下载到本地，解压cmd切换至解压目录，运行如下命令即可

​	版本查看：consul --version

​	启动：consul agent -dev

​	访问：localhost:8500

#### 集成案列

**服务者集成**

​	pom文件关键依赖

```xml
<!--springboot整合consul客户端-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
</dependency>
<dependency>
    <groupId>com.wj.springcloud</groupId>
    <artifactId>cloud-api-commons</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

yml文件

```
server:
  port: 8006

spring:
  application:
    name: cloud-payment-service
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        service-name: ${spring.application.name}
```

启动类

```java
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentConMain {
    public static void main(String[] args) {
        SpringApplication.run(PaymentConMain.class, args);
    }
}
```

控制类

```java
@RestController
public class PaymentConController {
    @Value("${server.port}")
    private String serverPort;

    @RequestMapping(value = "/payment/consul")
    public String getPaymentCon(){
        return "SpringCloud With Consul Port : " + serverPort + "\t" + UUID.randomUUID().toString();
    }
}
```

测试：localhost:8006/payment/consul

参考案列：cloud-provider-payment-c-8006

**消费者注册**

​	集成方式与服务者集成一致，除了yml文件修改端口以及服务名称。

​	参考案列：cloud-comsumer-order-con

### RestTemplate类

#### getForObject方法

​	getForObject(...):返回结果为响应体重的数据转化成的对象。可以理解为json;

#### getForEntity方法

​	getForEntity(...):返回对象为ResponseEntity对象，包含了响应中的一些重要信息，比如响应头，响应状态码、响应体等。

​	

### Ribbon（服务调用）

#### 基础

​	Springcloud ribbon是基于NetFlix Ribbon实现的一套客户端 负载均衡工具。相当于:Resttemplate + 负载均衡

​	Ribbon是NetFlix发布的开源项目，主要功能是提供客户端的软件爱你负载均衡算法与服务调用。Ribbon客户端组件提供一系列完善的配置如连接超时，重试等。简单的说就是在配置文件中列出Load Balancer(简称LB)后面所有的机器，Ribbon会自动的帮助你基于某种规则(如简单轮询，随机连接等)去连接这些机器。很容易使用Ribbon实现自定义的负载均衡算法。

​	**Ribbon负载均衡算法**：rest接口第几次请求数 % 服务器集群数 = 实际调用服务器位置的下标。每次服务重启请求数会从新记数。

#### IRule接口

​	Ribbon除了自身携带的均衡策略外，还提供了IRule接口来实现均衡策略，根据特定的算法从服务列表中选取一个要访问的服务。

​	存在7中算法实现，分别对应不同的实现类

​	RoundRobinRule:轮询机制

​	RandomRule:随机

​	RetryRule:先按照RoundRobinRule的策略获取服务，如果获取服务失败则在指定的事件内重置

​	WeightedResponseTimeRule:对RoundRobinRule的扩展，响应速度越快的实例选择权重越大，越统一被选择

​	BestAvailableRule:会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，而后选择一个并发量小的服务

​	AvailabilityFulteringRule:先过滤掉故障服务器，在选择并发量少的实例

​	ZoneAvoidanceRule:默认规则，复合判断server所在区域的性能和server的可用性能选择服务器

**替换IRule的算法(Ribbon的负载规则)**

案列：cloud-consumer-order

注意细节：自定义配置类不能放在@ComponentSacn所扫描的当前包以及子包下，否则将会被所有的Ribbon客户端所共享。

1、创建配置内MyselfRule.java

```java
@Configuration
public class MyselfRule {
    @Bean
    public IRule myRule(){
        return new RandomRule();// 定义为随机
    }
}
```

2、在启动类上加入@RibbonClient注解,指定服务与配置文件MyselfRule.class

```java
@SpringBootApplication
@EnableEurekaClient
@RibbonClient(name = "CLOUD-PAYMENT-SERVICE",configuration = MyselfRule.class)
public class OrderMain {
    public static void main(String[] args) {
        SpringApplication.run(OrderMain.class, args);
    }
}
```

#### 手写负载均衡算法

1、ApplicationContextBean去掉注解@LoadBalanced，作用对象cloud-consumer-order

2、LoadBalanced接口

```java
public interface LoadBalancer {
    ServiceInstance instance(List<ServiceInstance> serviceInstances);
}
```

3、Mylb

```java
@Component
public class MyLB implements LoadBalancer {
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public final int getAndIncrement(){
        int current;
        int next;
        do {
            current = this.atomicInteger.get();
            next = current >= 2147483647 ? 0 : current + 1;
        } while (!this.atomicInteger.compareAndSet(current, next));
        System.out.println("====>> Next ：" + next);
        return next;
    }

    @Override
    public ServiceInstance instance(List<ServiceInstance> serviceInstances) {
        int index = getAndIncrement() % serviceInstances.size();
        return serviceInstances.get(index);
    }
}
```

4、OrderController

```java
    @GetMapping(value = "/consumer/payment/lb")
    public String getPaymentLb(){
        List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
        if (instances == null || instances.size() < 0){
            return null;
        }

        ServiceInstance serviceInstance = loadBalancer.instance(instances);
        URI uri = serviceInstance.getUri();
        // /payment/lb获取端口
        return restTemplate.getForObject(uri + "/payment/lb", String.class);

    }
```

5、测试 http://localhost/consumer/payment/lb

### OpenFeign(服务调用)

官网地址：https://cloud.spring.io/spring-cloud-static/spring-cloud-openfeign/2.2.2.RELEASE/reference/html/

​	Frign是一个申明式WebService客户端。使用Feign能让编写WebService客户端更加简单。它使用的方法定义一个服务接口而后在上面添加注解。Feign也能支持可拔插式的编码器和解码器。Spring Cloud对Feign进行了封装，使其支持了SpringMvc标准注解和HTTPMessageConverters。Feign可以与Eureka和Ribbon组合使用以支持负载均衡。

​	**该技术使用在Client(消费端)**

#### OpenFeign的使用

1.接口+注解(@FeignClient)，PaymentFeignService接口类

```java
@Component
@FeignClient(value = "CLOUD-PAYMENT-SERVICE")
public interface PaymentFeignService {
    // 服务端对应的方法（访问url与方法名称要对应上）
    @GetMapping(value = "/payment/get/{id}")
    public CommonResult getPaymentById(@PathVariable("id") Long id);
}
```

2.新建module项目，cloud-consumer-order-feign

3.pom配置类编写，加入关键依赖其他依赖可参考项目

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>com.wj.springcloud</groupId>
    <artifactId>cloud-api-commons</artifactId>
    <version>${project.version}</version>
</dependency>
```

4.YML配置文件配置

```yml
server:
  port: 80

eureka:
  client:
    # 表示是否将自己注册进EurekaServer，默认为true
    register-with-eureka: false
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/ #集群版
```

5.启动类，加入@EnableFeignClients注解

```
@SpringBootApplication
@EnableFeignClients
public class OrderFeignMain {
    public static void main(String[] args) {
        SpringApplication.run(OrderFeignMain.class, args);
    }
}
```

6.业务类OrderFeignController.java

```
@RestController
public class OrderFeignController {
    @Resource
    private PaymentFeignService paymentFeignService;

    @GetMapping("/consumer/payment/getForEntity/{id}")
    public CommonResult<Payment> getPayment_(@PathVariable("id") Long id){
        return paymentFeignService.getPayment_(id);
    }
}
```

7.测试http://localhost/consumer/payment/getForEntit/1

#### OpenFeign超时控制

​	openfeign默认等待1秒钟，1秒钟过后报错。需要在yml文件中开启openfeign的等待时间，将等待服务端的时间设置为实际可行的时间。

```yml
server:
  port: 80
...# Eureka配置（略）
# 设置OpenFeign客户端超时时间，默认支持Ribbon
ribbon:
  # 指的是建立连接所用的时间，适用于网络状态正常的情况下，两端连接所用的时间
  ReadTimeout: 5000
  # 指的是建立连接后从服务器读取到可用资源所用的时间
  ConnectTimeout: 5000
```

注：作用于客户端cloud-consumer-order-feign

#### OpenFeign日志打印功能

openfeign的日志级别分为如下：

​	NONE:默认级别，不显示任何日志。

​	BASIC:仅记录请求方法，URL、响应状态码以及执行时间

​	HEADERS:除了BASIC中定义的信息外，还有请求和响应头信息

​	FULL:除了HEADERS中定义的信息外，还有请求和响应的正文与元数据

具体实现操作

​	1、编写配置类

```java
import feign.Logger;
@Configuration
public class FeignLogConfig {
    @Bean
    Logger.Level feignLoggerLevel(){
        return  Logger.Level.FULL;
    }
}
```

​	2、编写yml配置类，开启Feign的日志功能，往yml新增如下配置

```yml
logging:
  level: 
    # feign日志以什么级别监控在哪个接口上
    com.wj.springcloud.service.PaymentFeignService: debug
```

注：作用于客户端cloud-consumer-order-feign

### Hystrix(服务降级)

#### 基础知识

​	Hystrix是一个用于处理分布式系统的延迟和容错的开源库，在分布式系统里，许多依赖不可避免的会调用失败，比如超时、异常等，Hystrix能保证在一个依赖出问题的情况下，**不会导致整体服务的失败，以提高分布式系统的弹性**。

​	断路器本身是一种开关装置，当某个服务单元发生故障后，通过断路器的故障控制（类似熔断保险丝）。**向调用方返回一个符合预期的、可处理的备用响应（FallBack）,而不是长时间的等待或者抛出调用方法无法处理的异常**。这样就保证了服务调用方的线程不会被长时间、不必要的占用，从而避免了故障在分布式系统的蔓延以及雪崩。

​	Hystrix用于服务降级、服务熔断、接近实时监控等功能。

​	官网地址：github.com/Netflix/Hystrix/wiki/How-To-Use

​	**服务降级**：服务器繁忙，稍后再试，不让客户端等待并返回一个友好提示（FallBack）。在这些情况下会发生服务降级，比如：程序运行异常，超时，服务熔断、线程池/信号量打满

​	**服务熔断**：服务器达到最大访问，直接拒绝访问（Break），然后调用服务降级的方法并返回友好提示。

​	服务限流：秒杀高并发操作等，排队进行，一秒N个，有序进行（FallTimeout）。

#### 集成案列

​	**服务端**

​	1、新建module工程cloud-provider-payment-hystrix-8001

​	2、配置pom文件，加入关键依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>com.wj.springcloud</groupId>
    <artifactId>cloud-api-commons</artifactId>
    <version>${project.version}</version>
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

​	3、加入yml配置文件

```yml
server:
  port: 8001

spring:
  application:
    name: cloud-payment-service-hystrix

eureka:
  client:
    # 表示是否将自己注册进EurekaServer，默认为true
    register-with-eureka: true
    # 是否从EurekaServer中抓取已有的注册信息，默认为true。单节点无所谓，集群必须设置为true,这样才能配合ribbon使用负载均衡
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:7001/eureka/ #单机版
```

​	4、编写启动类

```java
@SpringBootApplication
public class PaymentHystrixMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentHystrixMain8001.class, args);
    }
}
```

​	5、编写业务类service与controller

```java
@Service
public class PaymentService {
    public String paymentInfo_Ok(Integer id){
        return " paymentInfo_Ok" + "-" + id;
    }
    public String paymentInfo_Timeout(Integer id){
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return " paymentInfo_Timeout" + "-" + id;
    }
}

@RestController
public class PaymentController {
    @Resource
    private PaymentService paymentService;
    @Value("${server.port}")
    private String serverPort;
    @GetMapping(value = "/payment/hystrix/ok/{id}")
    public String paymentInfo_Ok(@PathVariable("id") Integer id){
        return paymentService.paymentInfo_Ok(id);
    }
    @GetMapping(value = "/payment/hystrix/timeout/{id}")
    public String paymentInfo_Timeout(Integer id){
        return paymentService.paymentInfo_Timeout(id);
    }
}

```

​	6、测试,启动7001(单机版)与cloud-provider-payment-hystrix-8001

​	**客户端**

​	1、新建工程cloud-consumer-order-hystrix

​	2、编写pom依赖，与工程cloud-consumer-order-feign一致。

​	3、编写yml配置文件

```yml
server:
  port: 80
eureka:
  client:
    # 表示是否将自己注册进EurekaServer，默认为true
    register-with-eureka: false
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/
```

​	4、编写启动类，与工程cloud-consumer-order-feign一致。

​	5、编写openfeign的接口

```java
@Component
@FeignClient(value = "CLOUD-PAYMENT-SERVICE-HYSTRIX")
public interface PaymentService {
    @GetMapping(value = "/payment/hystrix/ok/{id}")
    public String paymentInfo_Ok(@PathVariable("id") Integer id);

    @GetMapping(value = "/payment/hystrix/timeout/{id}")
    public String paymentInfo_Timeout(@PathVariable("id")Integer id);
}
```

​	6、编写调用类

```java
@RestController
public class OrderHystrixController {
    @Resource
    public PaymentService paymentService;
    @GetMapping(value = "/consumer/payment/hystrix/ok/{id}")
    public String paymentInfo_Ok(@PathVariable("id") Integer id){
        return paymentService.paymentInfo_Ok(id);
    }
    @GetMapping(value = "/consumer/payment/hystrix/timeout/{id}")
    public String paymentInfo_Timeout(@PathVariable("id")Integer id){
        return paymentService.paymentInfo_Timeout(id);
    }
}
```

​	7、测试localhost/consumer/payment/hystrix/ok/1

#### 实际生产问题

​	Tomcat线程池中线程已满，访问响应缓慢，。

​	服务端超时导致服务器变慢，超时不在等待，提供服务降级。

​	服务端宕机出错，返回对应的友好提示，提供服务降级。

​	客户端访问问题或者客户端等待时间小于服务提供者，自己处理降级。

#### 服务降级

​	**服务端**（cloud-provider-payment-hystrix-8001）

​	1、服务端降级处理，加入@HystrixCommand注解，在服务端，服务类PaymentService类中加入如下代码

```java
    @HystrixCommand(fallbackMethod = "paymentInfo_TimeoutHandler",commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    })
    public String paymentInfo_Timeout(Integer id){
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return " paymentInfo_Timeout" + "-" + id;
    }

    public String paymentInfo_TimeoutHandler(Integer id){
        return "紧急处理!";
    }
```

​	注：@HystrixCommand标明降级后处理的方法，HystrixProperty表示处理什么问题时降级(异常、超时等),**方法的参数要一致**。

​	2、主启动类加入@EnableCiruitBreaker

```java
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class PaymentHystrixMain8001 {
    public static void main(String[] args) {
        SpringApplication.run(PaymentHystrixMain8001.class, args);
    }
}
```

**客户端**（cloud-consumer-order-hystrix）

​	1、客户端降级处理，需要在yml中开启服务降级配置，新增如下配置

```yml
feign:
  hystrix:
    enabled: true
```

​	2、启动类加入@EnableHystrix注解

```java
@SpringBootApplication
@EnableFeignClients
@EnableHystrix
public class OrderHystrixMain {
    public static void main(String[] args) {
        SpringApplication.run(OrderHystrixMain.class, args);
    }
}
```

​	3、业务类处理OrderHystrixController.java

```java
  
@GetMapping(value = "/consumer/payment/hystrix/timeout/{id}")
    @HystrixCommand(fallbackMethod = "paymentInfo_TimeoutHandler",commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1500")
    })
    public String paymentInfo_Timeout(@PathVariable("id")Integer id){
        return paymentService.paymentInfo_Timeout(id);
    }

    public String paymentInfo_TimeoutHandler(@PathVariable("id") Integer id){
        return "客户端紧急处理!";
    }
```

**全局服务降级**

​	一个方法一个降级处理会造成代码多余，使用@DefaultProperties(defaultFallback = "")注解，作用在类上，指定该类默认降级方法。

```java
@RestController
@DefaultProperties(defaultFallback = "payment_global_fallback")
public class OrderHystrixController {
    @Resource
    public PaymentService paymentService;

    @GetMapping(value = "/consumer/payment/hystrix/ok/{id}")
    @HystrixCommand // 使用全局紧急方法
    public String paymentInfo_Ok(@PathVariable("id") Integer id){
        return paymentService.paymentInfo_Ok(id);
    }


    @GetMapping(value = "/consumer/payment/hystrix/timeout/{id}")
    @HystrixCommand(fallbackMethod = "paymentInfo_TimeoutHandler",commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1500")
    }) // 使用自身指定的紧急方法，该方式与全局指定降级方法会产生冲突
    public String paymentInfo_Timeout(@PathVariable("id")Integer id){
        return paymentService.paymentInfo_Timeout(id);
    }

    public String paymentInfo_TimeoutHandler(@PathVariable("id") Integer id){
        return "客户端紧急处理!";
    }

    public String payment_global_fallback(@PathVariable("id") Integer id){
        return "全局紧急处理!";
    }
}
```

​	客户端针对服务端问题进行降级处理。客户端新增实现类PaymentFallbackService处理openfeign接口中方法，实现服务降级处理。同时在openfeign接口指定客户端实现类PaymentFallbackService。

```java
@Component
public class PaymentFallbackService implements PaymentService{
    @Override
    public String paymentInfo_Ok(Integer id) {
        return "paymentInfo_Ok客户端服务降级处理";
    }

    @Override
    public String paymentInfo_Timeout(Integer id) {
        return "paymentInfo_Timeout客户端服务降级处理";
    }
}
```

```java
@Component
@FeignClient(value = "CLOUD-PAYMENT-SERVICE-HYSTRIX",fallback = PaymentFallbackService.class)
public interface PaymentService {
    @GetMapping(value = "/payment/hystrix/ok/{id}")
    public String paymentInfo_Ok(@PathVariable("id") Integer id);

    @GetMapping(value = "/payment/hystrix/timeout/{id}")
    public String paymentInfo_Timeout(@PathVariable("id")Integer id);
}

```

#### 服务熔断

​	熔断机制是应对雪崩效应的一种微服务链路保护机制。当扇出链路的某个微服务出错不可用或者响应时间太长时，会会进行服务降级，进入熔断该节点微服务的调用，快速返回错误的响应信息。

​	当检测到该节点微服务调用响应正常后，恢复调用链路。

​	熔断机制是通过Hystrix实现，Hystrix会监控微服务间的调用情况，当失败的调用到一定阀值，缺省是5秒内20次调用失败，就会启动熔断机制。当阀值回复到正常时，熔断机制关闭服务回复正常。

​	运行过程：服务降级》进而熔断》恢复调用链路

​	熔断机制的注解是@HyxtrixCommand

​	技术文章：marttinfowler.com/bliki/CircuitBreaker.html

**集成案例**（cloud-provider-payment-hystrix-8001）

1、编写对应服务类PaymentService，加入如下代码：

```java
@HystrixCommand(fallbackMethod = "paymentCircuitBreaker_fallback",commandProperties = {
    @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),// 是否开启断路器
    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),// 请求次数
    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),// 时间窗口期
    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60")// 失败率达到多少后，熔断
})
public String paymentCircuitBreaker(Integer id){
    if (id < 0) {
        throw new RuntimeException("id 不能为负数！");
    }
    String serialNumber = IdUtil.simpleUUID();
    return Thread.currentThread().getName() + "调用成功，流水号：" + serialNumber;
}

public String paymentCircuitBreaker_fallback(Integer id){
    return "id 不能为负数，请稍后再试！";
}
```

2、编写控制类PaymentController

```java
@GetMapping(value = "/payment/cricuit/{id}")
public String PaymentCricuitBreaker(@PathVariable("id") Integer id){
    return paymentService.paymentCircuitBreaker(id);
}
```

#### Hystrix图型管理

​	Hystrix提供了准实时的调用监控HystrixDashboard，当Hystrix会持续的记录所有通过Hystrix发起的请求执行信息，并以统计报表和图形的形式展示给用户。

**监控界面集成**

​	1、新建module项目cloud-consumer-hys-dashboard9001

​	2、添加pom依赖，在cloud-consumer-order-hystrix项目的pom基础上，新增如下依赖：

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
</dependency>
```

​	3、配置文件application.yml只添加server.port=9001即可。启动类代码如下：

```java
@SpringBootApplication
@EnableHystrixDashboard
public class HystrixDashboradMain {
    public static void main(String[] args) {
        SpringApplication.run(HystrixDashboradMain.class, args);
    }
}
```

​	4、所有需要监控的服务端都需要添加依赖监控，pom依赖如下：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

​	5、测试：localhost:9001/hystrix

​	6.服务端(cloud-provider-payment-hystrix-8001)启动类还需要加入一下代码

```java
    /**
     * 此配置是为服务监控而配置，与服务容错本身无关，Springclouds升级后出现的
     * ServletRegistrationBean因为Springboot的默认路径不是"/hystrix.stream"
     * 只要在自己的项目里配置上如下Servvlet即可
     */
    @Bean
    public ServletRegistrationBean getServlet(){
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
```

### Gateway(路由网关)

​	Geteway是在Spring生态系统之上构建的API网关服务，基于Spring5，Springboot2与Project Reactor等技术。Gateway目标在提供一种简单而有效的方式对API进行路由，以及提供一些强大的过滤功能，如：熔断、限流、重试等。

**能力**：反向代理、鉴权、流量控制、熔断、日志监控等

**三大核心**：Route(路由)，Predicate(断言)，Filter(过滤)，

​	路由：构建网关的基本模块，由ID,目标URI,一系列的断言与过滤器组成，如果断言为true则匹配该路由。

​	断言：开发人员可以匹配HTTP请求中的所有内容(请求头或者请求参数)，如果请求与断言相匹配则进行路由，可参考Predicate类（java8）.

​	过滤：Spring框架中的GatewayFilter的实例，使用过滤器可以在请求被路由前或者之后对请求进行修改。

​	路由原理：web请求，通过一些匹配条件定位到真正服务节点。并在这个转发过程前后进行一些精细化控制。predicate就是匹配的条件，而Filer可以理解为一个无所不能的烂机器，有了这2者再加上目标的URI就可以实现一个具体的路由。

**核心逻辑**：路由转发+执行过滤器链。

#### 集成案例

​	1、新建module项目cloud-gateway-gateway9527

​	2、为pom文件加入相关依赖

```xml
 <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<!--需要移除
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
-->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
</dependency>
...       
```

​	3、编写启动类

```java
@SpringBootApplication
@EnableEurekaClient
public class GateWayMain {
    public static void main(String[] args) {
        SpringApplication.run(GateWayMain.class, args);
    }
}
```

​	4、编写配置类

```yml
server:
  port: 9527

spring:
  application:
    name: cloud-gateway
  cloud:
    gateway:
      routes:
        - id: payment_routh # 路由的ID,没有固定的要求但要唯一，建议配合服务名
          uri: http://localhost:8085  # 匹配后提供服务的路由地址
          predicates:
            - Path=/payment/get/**   # 断言,路径相匹配的进行路由

        - id: payment_routh2
          uri: http://localhost:8085
          predicates:
            - Path=/payment/lb/**
eureka:
  instance:
    hostname: cloud-gateway-service
  client:
    service-url:
      register-with-eureka: true
      fetch-registry: true
      defaultZone: http://eureka7001.com:7001/eureka/
```

​	5、测试需要启动eureka7001,paymentment8085以及gateway9527三个项目。

#### Gateway网关配置

​	gateway的网关配置有2中方式，第一种通过yml配置文件配置，第二种，通过硬编码的形式。通过硬编码的形式需要添加配置类，代码如下：

```java
@Configuration
public class GateWayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder){
        RouteLocatorBuilder.Builder builder = routeLocatorBuilder.routes();
        builder.route("path_route3",    // 路由的id
                r -> r.path("com") // 访问路径     
                        .uri("http://www.baidu,com")    // 跳转路径
        ).build();
        return builder.build();
    }
    // 如果还有地址，需要新建一个方法返回RouteLocator
}
```

#### Gateway动态配置路由

​	默认情况下Gateway会根据注册中心的服务列表，以注册中心上的微服务名为路径创建动态路由进行转发，从而实现动态路由的功能。需要在cloud-gateway-gateway9527项目的yml配置文件加如下代码：

```yml
....
spring:
  application:
    name: cloud-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true #开启从注册中心动态创建路由功能，利用微服务名进行路由
      routes:
        - id: payment_routh # 路由的ID,没有固定的要求但要唯一，建议配合服务名
          #uri: http://localhost:8085  # 匹配后提供服务的路由地址
          uri: lb://cloud-payment-service # 匹配后提供微服务的路由地址
          predicates: #可参考predicate下面的配置
            - Path=/payment/get/**   # 断言,路径相匹配的进行路由
...
```

#### Predicate的配置

​	-- Path:# 断言,路径相匹配的进行路由

​	--After:在配置时间之后断言起作用，需要填写美国时间格式

​	--Between:在什么时间段内起作用，时间用逗号隔开

​	--Cookie：cook范访问

​	--Method:方法限制，配置参数需要大写。

#### Filter的使用

​	Filter的使用可以在yml文件中配置，如下代码。

```yml
....
spring:
  application:
    name: cloud-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true #开启从注册中心动态创建路由功能，利用微服务名进行路由
      routes:
        - id: payment_routh # 路由的ID,没有固定的要求但要唯一，建议配合服务名
          uri: lb://cloud-payment-service # 匹配后提供微服务的路由地址
          filters:
          	-AddRequestHeader=X-Request-red,blue
...
```

​	自定义过滤器的使用，需要实现GlobalFilter,Ordered接口，如下：

```java
@Component
public class MyGatewayFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String user_name = exchange.getRequest().getQueryParams().getFirst("username");
        if (user_name == null) {
            System.out.println("UserName is NUll!");
            exchange.getResponse().setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            return  exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
```



