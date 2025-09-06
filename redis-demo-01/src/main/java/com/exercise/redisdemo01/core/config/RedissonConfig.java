package com.exercise.redisdemo01.core.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类，用于连接到Redis
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/9
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedissonConfig {
    /**
     * Redis主机地址
     */
    private String host;
    /**
     * Redis端口号
     */
    private String port;

    /**
     * Redis数据库索引
     */
    private int database;

    /**
     * 创建Redisson配置对象
     *
     * @return Redisson配置对象
     */
    @Bean
    public Config redissonClientConfig() {
        String address = "redis://" + host + ":" + port;

        Config config = new Config();
        config.useSingleServer().setAddress(address);
        config.useSingleServer().setDatabase(database);
        log.info("Redisson连接地址：{}", address);
        log.info("Redisson连接数据库：{}", database);
        return config;
    }

    /**
     * 创建Redisson客户端对象
     *
     * @return Redisson客户端对象
     */
    @Bean
    public RedissonClient redissonClient() {
        RedissonClient redissonClient = Redisson.create(redissonClientConfig());
        log.info("Redisson连接成功！");
        return redissonClient;
    }
}
