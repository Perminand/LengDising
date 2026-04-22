package com.landscape.design.config;

import com.landscape.design.domain.SubscriptionTier;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.subscription")
public class SubscriptionProperties {

    private TierLimits free = new TierLimits();
    private TierLimits basic = new TierLimits();
    private TierLimits pro = new TierLimits();

    @Getter
    @Setter
    public static class TierLimits {
        private int maxProjects = 1;
        private int maxSceneElements = 20;
    }

    public TierLimits limitsFor(SubscriptionTier tier) {
        return switch (tier) {
            case FREE -> free;
            case BASIC -> basic;
            case PRO -> pro;
        };
    }
}
