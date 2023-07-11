package com.github.qyl.controller;

import com.github.qyl.annotation.RateLimit;
import com.github.qyl.constant.RateLimitModel;
import com.github.qyl.dto.User;
import com.github.qyl.model.TokenBucketLuaScript;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class RateLimterTestController {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 令牌桶
     * @return
     */
    @GetMapping("/get5")
    @RateLimit(rate = 10, rateInterval = "10s", quantity = 3, maxQuantity = 10, model = RateLimitModel.TOKEN_BUCKET)
    public String get5(String name) {
        return "get5";
    }

    /**
     * 令牌桶
     * @return
     */
    @GetMapping("/get")
    public Map<String, String> get() {
        List<Object> keys = new ArrayList<>();
        keys.add("test_RateLimter");//标识
        keys.add(10);//最大容量
        keys.add(10);//时间窗口内的限额
        keys.add(60);//时间窗口大小（秒）
        keys.add(5);//需要的令牌数量，默认为1
        List<Long> results = redissonClient.getScript().eval(RScript.Mode.READ_WRITE, TokenBucketLuaScript.getTokenBucketLuaScript(), RScript.ReturnType.MULTI, keys);
        Map<String, String> map = new HashMap<String, String>(3);
        map.put("是否限流", results.get(0) == 0 ? "未限流" :"已限流");
        map.put("容量", results.get(1).toString());
        map.put("当前剩余容量", results.get(2).toString());
        return map;
    }

    @GetMapping("/get2")
    @RateLimit(rate = 2, rateInterval = "10s",rateExpression = "${spring.ratelimiter.max:2}")
    public String get2() {
        return "get";
    }

    @GetMapping("/get3")
    @RateLimit(rate = 1, rateInterval = "10s")
    public String get3(String name) {
        return "get";
    }

    @GetMapping("/get4")
    @RateLimit(rate = 1, rateInterval = "10s", fallbackFunction = "getFallback")
    public String get4(String name) {
        return "get";
    }

    /**
     * 压测的接口
     */
    @GetMapping("/ab")
    @RateLimit(rate = 100000000, rateInterval = "30s")
    public String wrk() {
        return "get";
    }

    @PostMapping("/user")
    @RateLimit(rate = 5, rateInterval = "10s",keys = {"#user.name","user.id"})
    public String user(@RequestBody User user) {
        return "user";
    }


    public String getFallback(String name){
        return "命中了" + name;
    }

    public String keyFunction(String name) {
        return "keyFunction";
    }
}
