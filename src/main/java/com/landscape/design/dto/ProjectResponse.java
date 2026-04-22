package com.landscape.design.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        String sceneState,
        Instant createdAt,
        Instant updatedAt
) {}
