package com.landscape.design.web;

import com.landscape.design.config.AppDemoProperties;
import com.landscape.design.domain.User;
import com.landscape.design.dto.ChangeTierRequest;
import com.landscape.design.dto.UserInfoDto;
import com.landscape.design.security.LandscapeUserPrincipal;
import com.landscape.design.service.SubscriptionService;
import com.landscape.design.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final AppDemoProperties demoProperties;

    @GetMapping
    public UserInfoDto me(@AuthenticationPrincipal LandscapeUserPrincipal principal) {
        User user = userRepository.findById(principal.getId()).orElseThrow();
        return new UserInfoDto(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                subscriptionService.effectiveTier(user).name()
        );
    }

    @PostMapping("/subscription")
    public UserInfoDto changeSubscription(
            @AuthenticationPrincipal LandscapeUserPrincipal principal,
            @Valid @RequestBody ChangeTierRequest request
    ) {
        if (!demoProperties.isAllowTierChange()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Смена тарифа через API отключена");
        }
        User user = userRepository.findById(principal.getId()).orElseThrow();
        Instant end = null;
        if (request.getPeriodEndIso() != null && !request.getPeriodEndIso().isBlank()) {
            end = Instant.parse(request.getPeriodEndIso());
        }
        subscriptionService.setTier(user, request.getTier(), end);
        return new UserInfoDto(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                subscriptionService.effectiveTier(user).name()
        );
    }
}
