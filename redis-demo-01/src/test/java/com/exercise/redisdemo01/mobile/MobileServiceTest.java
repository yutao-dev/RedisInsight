package com.exercise.redisdemo01.mobile;

import com.exercise.redisdemo01.mobile.service.MobileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 测试MobileService
 *
 * @author 王玉涛
 * @version 1.0
 * @since 2025/8/12
 */
@Slf4j
@SpringBootTest
class MobileServiceTest {

    @Resource
    private MobileService mobileService;

    @Test
    void testBuyMobile() {
        String mobile = mobileService.buyMobile("小米", 5000);
        log.info("手机购买结果：{}", mobile);
    }
}
