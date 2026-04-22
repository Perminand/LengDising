package com.landscape.design.repository;

import com.landscape.design.domain.Subscription;
import com.landscape.design.domain.SubscriptionStatus;
import com.landscape.design.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUserAndStatusOrderByCreatedAtDesc(User user, SubscriptionStatus status);

    Optional<Subscription> findFirstByUserAndStatusOrderByCreatedAtDesc(User user, SubscriptionStatus status);
}
