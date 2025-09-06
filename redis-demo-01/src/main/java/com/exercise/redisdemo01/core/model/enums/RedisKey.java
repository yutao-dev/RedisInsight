package com.exercise.redisdemo01.core.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Redis键枚举类
 * 用于定义系统中所有Redis键的前缀和描述信息
 * 可以根据业务进行扩充、添加
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/23
 */
@Getter
@AllArgsConstructor
public enum RedisKey {

    /**
     * 用户模块根键
     */
    USER("user:", "用户模块相关"),

    /**
     * 用户信息键
     * 格式: user:info:{userId}
     */
    USER_INFO("user:info:", "用户信息"),

    /**
     * 用户token键
     * 格式: user:token:{tokenId}
     */
    USER_TOKEN("user:token:", "用户token"),

    /**
     * 用户角色键
     * 格式: user:role:{userId}
     */
    USER_ROLE("user:role:", "用户角色"),

    /**
     * 用户权限键
     * 格式: user:permission:{userId}
     */
    USER_PERMISSION("user:permission:", "用户权限");

    /**
     * Redis键前缀
     */
    private final String keyPrefix;

    /**
     * 键描述信息
     */
    private final String description;

    /**
     * 静态方法：合并多个键段为一个完整的Redis键
     *
     * @param keys 键段数组
     * @return 合并后的Redis键字符串
     */
    public static String ketMerging(String... keys) {
        StringBuilder keyBuilder = new StringBuilder();
        return keyMerge(keyBuilder, keys);
    }

    /**
     * 实例方法：将当前枚举的前缀与传入的键段合并为完整Redis键
     *
     * @param keys 键段数组
     * @return 完整的Redis键字符串
     */
    public String keyAssembled(String... keys) {
        StringBuilder keyBuilder = new StringBuilder(this.keyPrefix);
        return keyMerge(keyBuilder, keys);
    }

    /**
     * 私有工具方法：执行键段合并操作
     *
     * @param keyBuilder StringBuilder对象，用于构建键字符串
     * @param keys       键段数组
     * @return 合并后的键字符串
     */
    private static String keyMerge(StringBuilder keyBuilder, String[] keys) {
        int length = keys.length;
        for (int i = 0; i < length; i++) {
            keyBuilder.append(keys[i]);
            if (i != length - 1) {
                keyBuilder.append(":");
            }
        }
        return keyBuilder.toString();
    }
}
