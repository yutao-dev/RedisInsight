package com.exercise.redisdemo01.mobile.model.rule;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

/**
 * 手机品牌折扣规则类，通过函数式接口实现策略模式的轻量级替换
 * 该类封装了不同品牌的折扣规则，支持动态匹配和计算折扣价格
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/12
 */
@Data
@AllArgsConstructor
public class MobileBrandAccountRule {
    /**
     * 折扣条件判断函数列表，每个函数接收价格参数，返回是否满足折扣条件
     */
    private List<Function<Integer, Boolean>> isAccounts;
    
    /**
     * 折扣率列表，与isAccounts一一对应，表示满足条件时的价格折扣倍数
     */
    private List<Double> accounts;
    
    /**
     * 优惠结果描述列表，与isAccounts一一对应，表示满足条件时的优惠说明
     */
    private List<String> accountResults;
    
    /**
     * 手机品牌名称
     */
    private String mobileBrand;
    
    /**
     * 根据手机价格和折扣规则计算购买结果
     * 遍历所有折扣规则，找到第一个满足条件的规则并应用折扣
     *
     * @param price 手机原价
     * @param sb    结果构建器，用于拼接购买结果信息
     * @return 包含购买结果信息的StringBuilder对象
     */
    public StringBuilder buyMobile(Integer price, StringBuilder sb) {
        int size = isAccounts.size();
        for (int i = 0; i < size; i++) {
            Function<Integer, Boolean> isAccount = isAccounts.get(i);
            Boolean inAccount = isAccount.apply(price);
            if (inAccount) {
                sb.append("手机购买成功, ")
                        .append("手机价格: ")
                        .append(Double.valueOf(price) * accounts.get(i))
                        .append(",")
                        .append("优惠附加: ")
                        .append(accountResults.get(i));
                return sb;
            }
        }
        return sb.append("手机购买失败");
    }
}
