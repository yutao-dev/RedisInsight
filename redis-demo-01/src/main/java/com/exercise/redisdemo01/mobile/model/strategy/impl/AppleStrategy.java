package com.exercise.redisdemo01.mobile.model.strategy.impl;

import com.exercise.redisdemo01.mobile.model.enums.MobileBrand;
import com.exercise.redisdemo01.mobile.model.strategy.MobileBrandStrategy;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 基于苹果品牌的策略方式
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/12
 */
@Data
@Component
public class AppleStrategy implements MobileBrandStrategy {

    private String brand = MobileBrand.APPLE.getBrand();

    /**
     * 手机购买策略
     *
     * @param price     手机价格
     * @param buyResult 手机购买结果
     * @return 手机购买结果
     */
    @Override
    public StringBuilder buyMobile(Integer price, StringBuilder buyResult) {
        // 苹果品牌，根据价格进行购买逻辑
        if (price <= 5000) {
            buyResult.append("购买苹果手机成功,").append("手机价格: ").append(price).append(" 附赠AirTag");
        } else if (price <= 8000) {
            buyResult.append("购买苹果手机成功,").append("手机价格: ").append(Double.valueOf(price) * 0.98D).append(" 12期免息");
        } else {
            buyResult.append("购买苹果手机成功,").append("手机价格: ").append(Double.valueOf(price) * 0.95D).append(" 24期免息 附赠MagSafe");
        }

        return buyResult;
    }
}
