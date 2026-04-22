package com.landscape.design.dto;

import java.util.UUID;

public record UserInfoDto(
        UUID id,
        String email,
        String displayName,
        String subscriptionTier
) {}
