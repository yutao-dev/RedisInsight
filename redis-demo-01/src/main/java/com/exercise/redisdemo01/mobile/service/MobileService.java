package com.exercise.redisdemo01.mobile.service;

import com.exercise.redisdemo01.mobile.factory.MobileBrandAccountRuleFactory;
import com.exercise.redisdemo01.mobile.factory.MobileStrategyFactory;
import com.exercise.redisdemo01.mobile.model.enums.MobileBrand;
import com.exercise.redisdemo01.mobile.model.rule.MobileBrandAccountRule;
import com.exercise.redisdemo01.mobile.model.strategy.MobileBrandStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 手机服务类
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MobileService {

    private final MobileStrategyFactory mobileStrategyFactory;

    /**
     * 购买手机
     *
     * @param mobileBrand 手机品牌
     * @param price       手机价格
     * @return 购买结果
     */
    public String buyMobile(String mobileBrand, Integer price) {
        StringBuilder buyResult = new StringBuilder();

        if (price == null) {
            buyResult.append("手机价格不能为空");
            return buyResult.toString();
        }

        MobileBrandAccountRule mobileBrandAccountRule = MobileBrandAccountRuleFactory.getMobileBrandAccountRule(mobileBrand);
        buyResult = Objects.isNull(mobileBrandAccountRule) ? buyResult.append("手机品牌不能为空") : mobileBrandAccountRule.buyMobile(price, buyResult);
        return buyResult.toString();
    }
}
