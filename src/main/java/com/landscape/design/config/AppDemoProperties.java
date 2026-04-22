package com.landscape.design.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.demo")
public class AppDemoProperties {

    /** Включает смену тарифа без платёжного шлюза (для разработки и демо). */
    private boolean allowTierChange = true;
}
