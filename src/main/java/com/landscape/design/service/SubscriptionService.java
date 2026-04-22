package com.landscape.design.service;

import com.landscape.design.config.SubscriptionProperties;
import com.landscape.design.domain.Subscription;
import com.landscape.design.domain.SubscriptionStatus;
import com.landscape.design.domain.SubscriptionTier;
import com.landscape.design.domain.User;
import com.landscape.design.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionProperties subscriptionProperties;

    @Transactional(readOnly = true)
    public SubscriptionTier effectiveTier(User user) {
        Optional<Subscription> active = subscriptionRepository
                .findFirstByUserAndStatusOrderByCreatedAtDesc(user, SubscriptionStatus.ACTIVE);
        if (active.isEmpty()) {
            return SubscriptionTier.FREE;
        }
        Subscription s = active.get();
        if (s.getCurrentPeriodEnd() != null && Instant.now().isAfter(s.getCurrentPeriodEnd())) {
            return SubscriptionTier.FREE;
        }
        return s.getTier();
    }

    public SubscriptionProperties.TierLimits limitsFor(User user) {
        return subscriptionProperties.limitsFor(effectiveTier(user));
    }

    @Transactional
    public void createDefaultFreeSubscription(User user) {
        Subscription sub = new Subscription();
        sub.setId(UUID.randomUUID());
        sub.setUser(user);
        sub.setTier(SubscriptionTier.FREE);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setCurrentPeriodEnd(null);
        subscriptionRepository.save(sub);
    }

    @Transactional
    public Subscription setTier(User user, SubscriptionTier tier, Instant periodEnd) {
        subscriptionRepository.findByUserAndStatusOrderByCreatedAtDesc(user, SubscriptionStatus.ACTIVE)
                .forEach(s -> {
                    s.setStatus(SubscriptionStatus.CANCELED);
                    subscriptionRepository.save(s);
                });
        Subscription sub = new Subscription();
        sub.setId(UUID.randomUUID());
        sub.setUser(user);
        sub.setTier(tier);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setCurrentPeriodEnd(periodEnd);
        return subscriptionRepository.save(sub);
    }
}
