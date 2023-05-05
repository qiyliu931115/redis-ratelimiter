
1.引入依赖

```
<dependency>
    <groupId>com.github.qyl</groupId>
    <artifactId>redis-ratelimiter</artifactId>
</dependency>
```


2.使用说明



使用前请确认项目已配置了spring.redis相关连接。


```
spring:
    redis:
        host: 192.168.7.94
        port: 6379
        password: 123456
```



application.yml配置开启限流功能：


```
spring:
    ratelimiter:
        enabled: true
```


在需要加限流逻辑的方法上，添加注解 @RateLimit，如：


```
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    @RateLimit(rate = 5, rateInterval = "10s")
    public String get(String name) {
        return "hello";
    }
}
```

@RateLimit 注解说明

```
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 时间窗口流量数量
     * @return rate
     */
    long rate();

    /**
     * 时间窗口流量数量表达式
     * @return rateExpression
     */
    String rateExpression() default "";

    /**
     * 时间窗口，最小单位秒，如 2s，2h , 2d
     * @return rateInterval
     */
    String rateInterval();

    /**
     * 获取key
     * @return keys
     */
    String [] keys() default {};

    /**
     * 限流后的自定义回退后的拒绝逻辑
     * @return fallback
     */
    String fallbackFunction() default "";

    /**
     * 自定义业务 key 的 Function
     * @return key
     */
    String customKeyFunction() default "";

    /**
     * 每次请求令牌的数量(令牌桶模式参数) 默认为1
     * @return quantity
     */
    long quantity() default 1L;

    /**
     * 令牌桶的容量(令牌桶模式参数) 默认为10
     * @return quantity
     */
    long maxQuantity() default 10L;

    /**
     * 限流模式 默认是固定窗口计数器模式
     * @return
     */
    RateLimitModel model() default RateLimitModel.COUNT;

}
```

@RateLimit 注解可以添加到任意被 spring 管理的 bean 上，不局限于 controller ,service 、repository 也可以。在最基础限流功能使用上，以上三个步骤就已经完成了。@RateLimit 有两个最基础的参数，rateInterval 设置了时间窗口，rate 设置了时间窗口内允许通过的请求数量



限流策略


目前使用了两种限流策略， 默认是固定时间计数器，另一种是令牌桶，这边简单整理下这两种限流策略：



固定窗口计数器:

```
使用固定窗口实现限流的思路大致为，
将某一个时间段当做一个窗口，在这个窗口内存在一个计数器记录这个窗口接收请求的次数，
每接收一次请求便让这个计数器的值加一,如果计数器的值大于请求阈值的时候，即开始限流。
当这个时间段结束后，会初始化窗口的计数器数据，相当于重新开了一个窗口重新监控请求次数。
```

令牌桶:

```
令牌桶算法是按固定速率生成令牌，请求能从桶中拿到令牌就执行，否则执行失败。
漏桶算法是任务进桶速率不限，但是出桶的速率是固定的，超出桶大小的的任务丢弃，也就是执行失败。
```

面对瞬时大流量，该算法可以在短时间内请求拿到大量令牌，而且拿令牌的过程并不是消耗很大的事情。

令牌桶算法能够保持稳定的流量上限，同时也允许偶尔的流量爆发。



```
/**
* 固定时间计数器（rate=1， rateInterval=10s, 在第一次请求开始后十秒内只允许一个请求）
* @param name
* @return
  */
  @GetMapping("/rateLimit")
  @RateLimit(rate = 1, rateInterval = "10s", fallbackFunction = "getFallback")
  public ResponseMessage rateLimit(String name) {
  return ResponseMessage.ok("rateLimit");
  }
```

```
/**
* 令牌桶（rate=10， rateInterval=10s, 在10秒内会匀速生成10个令牌，也就是说在第一次请求开始后10秒内每秒会生成1个令牌，maxQuantity=10 令牌桶最大容量是10，超过10后生成的令牌会被丢弃，quantity=3 每次请求需要从令牌桶里拿出消耗3个令牌）
* @return
  */
  @GetMapping("/tokenBucket")
  @RateLimit(rate = 10, rateInterval = "10s", quantity = 3, maxQuantity = 10, model = RateLimitModel.TOKEN_BUCKET, fallbackFunction = "getFallback")
  public ResponseMessage tokenBucket(String name) {
  return ResponseMessage.ok("tokenBucket");
  }
```

```
/**
* 降级策略
*
* @param name
* @return
  */
  public ResponseMessage getFallback(String name){
  return ResponseMessage.ok("你被限流了");
  }
```

限流的粒度，限流 key


限流的粒度是通过限流的 key 来做的，在最基础的设置下，限流的 key 默认是通过方法名称拼出来的，规则如下：


```
key = RateLimiter_ + 类名 + 方法名
```

触发限流后的行为


默认触发限流后 程序会返回一个 http 状态码为 429 的响应，响应值如下：


```
{
"code": 429,
"msg": "Too Many Requests"
}
```

如果项目中有全局异常捕获，需要在配置代码中增加捕获RateLimitException的逻辑，示例如下：

```
@ControllerAdvice
@Slf4j
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
@Order(KiafExceptionHandler.Order.GLOBAL_PRECEDENCE)
public class KiafExceptionHandler implements ResponseBodyAdvice {


	@ExceptionHandler(RateLimitException.class)
	@ResponseBody
	public ResponseMessage handleRateLimitException(RateLimitException oplfe) {
		//具体处理逻辑
    	log.error(oplfe.getMessage(), oplfe);
    	return ResponseGenerator.genFailResult(oplfe.getMessage());
	}


}
```

3.进阶用法
自定义限流的 key


自定义限流 key 有三种方式，当自定义限流的 key 生效时，限流的 key 就变成了（默认的 key + 自定义的 key）。下面依次给出示例



@RateLimitKey 的方式

```
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    @RateLimit(rate = 5, rateInterval = "10s")
    public String get(@RateLimitKey String name) {
        return "get";
    }
}
```

@RateLimitKey 注解可以放在方法的入参上，要求入参是基础数据类型，上面的例子，如果 name = kl。那么最终限流的 key 如下：


key = RateLimiter_com.github.qyl.controller.RateLimterTestController.get-kl


指定 keys 的方式

```
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    @RateLimit(rate = 5, rateInterval = "10s",keys = {"#name"})
    public String get(String name) {
        return "get";
    }

    @GetMapping("/hello")
    @RateLimit(rate = 5, rateInterval = "10s",keys = {"#user.name","user.id"})
    public String hello(User user) {
        return "hello";
    }
}
```

keys 这个参数比 @RateLimitKey 注解更智能，基本可以包含 @RateLimitKey 的能力，只是简单场景下，使用起来没有 @RateLimitKey 那么便捷。keys 的语法来自 spring 的 Spel，可以获取对象入参里的属性，支持获取多个，最后会拼接起来。使用过 spring-cache 的同学可能会更加熟悉 如果不清楚 Spel 的用法，可以参考 spring-cache 的注解文档。


自定义 key 获取函数

```
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    @RateLimit(rate = 5, rateInterval = "10s",customKeyFunction = "keyFunction")
    public String get(String name) {
        return "get";
    }

    public String keyFunction(String name) {
        return "keyFunction" + name;
    }
}
```

当 @RateLimitKey 和 keys 参数都没法满足时，比如入参的值是一个加密的值，需要解密后根据相关明文内容限流。可以通过在同一类里自定义获取 key 的函数，这个函数要求和被限流的方法入参一致，返回值为 String 类型。返回值不能为空，为空时，会回退到默认的 key 获取策略。



自定义限流后的行为

```
spring:
ratelimiter:
enabled: true
status-code: 555
response-body: {"code":555,"msg":"Config Response Too Many Requests"}
```

添加如上配置后，触发限流时，http 的状态码就变成了 555。响应的内容变成了 Config Response Too Many Requests 了





自定义触发限流处理函数，限流降级

```
@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    @RateLimit(rate = 5, rateInterval = "10s",fallbackFunction = "getFallback")
    public String get(String name) {
        return "get";
    }

    public String getFallback(String name){
        return "Too Many Requests" + name;
    }

}
```

要求请求参数喝返回值的类型需要和原限流函数的返回值类型一致，当触发限流时，框架会调用 fallbackFunction 配置的函数执行并返回，达到限流降级的效果

动态设置限流大小


在 @RateLimit 注解里新增了属性 rateExpression。该属性支持 Spel 表达式从 Spring 的配置上下文中获取值。 当配置了 rateExpression 后，rate 属性的配置就不生效了。使用方式如下：


```
    @GetMapping("/get2")
    @RateLimit(rate = 2, rateInterval = "10s",rateExpression = "\${spring.ratelimiter.rate}")
    public String get2() {
        return "get";
    }
```

```
spring:
  ratelimiter:
    rate: 1
```

