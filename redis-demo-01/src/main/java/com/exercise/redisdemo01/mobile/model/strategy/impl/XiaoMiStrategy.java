package com.exercise.redisdemo01.mobile.model.strategy.impl;

import com.exercise.redisdemo01.mobile.model.enums.MobileBrand;
import com.exercise.redisdemo01.mobile.model.strategy.MobileBrandStrategy;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 基于小米品牌实现的策略类
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/12
 */
@Data
@Component
public class XiaoMiStrategy implements MobileBrandStrategy {

    private String brand = MobileBrand.XIAOMI.getBrand();

    /**
     * 手机购买策略
     *
     * @param price     手机价格
     * @param buyResult 购买结果
     * @return 手机购买结果
     */
    @Override
    public StringBuilder buyMobile(Integer price, StringBuilder buyResult) {
        // 小米品牌，根据价格进行购买逻辑
        if (price <= 1000) {
            buyResult.append("购买小米手机成功,").append("手机价格: ").append(Double.valueOf(price) * 0.98D);
        } else if (price <= 2000) {
            buyResult.append("购买小米手机成功,").append("手机价格: ").append(Double.valueOf(price) * 0.95D).append(" 附赠有线耳机");
        } else {
            buyResult.append("购买小米手机成功,").append("手机价格: ").append(Double.valueOf(price) * 0.9D).append(" 附赠小米手环");
        }

        return buyResult;
    }
}
