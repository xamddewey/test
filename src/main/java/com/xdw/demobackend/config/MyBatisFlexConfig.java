package com.xdw.demobackend.config;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Flex 配置类
 * 用于自定义 MyBatis Flex 的配置
 */
@Configuration
public class MyBatisFlexConfig implements MyBatisFlexCustomizer {

    @Override
    public void customize(FlexGlobalConfig flexGlobalConfig) {
        AuditManager.setAuditEnable(true);
    }
}
