package com.github.qyl;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = RateLimiterProperties.PREFIX)
public class RateLimiterProperties {

    public static final String PREFIX = "spring.ratelimiter";

    private ClusterServer redisClusterServer;

    private int statusCode = 429;

    private String responseBody = "{\"code\":429,\"msg\":\"Too Many Requests\"}";


    public static class ClusterServer{

        private String[] nodeAddresses;

        public String[] getNodeAddresses() {
            return nodeAddresses;
        }

        public void setNodeAddresses(String[] nodeAddresses) {
            this.nodeAddresses = nodeAddresses;
        }
    }
}
