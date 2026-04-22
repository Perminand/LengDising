package com.landscape.design.dto;

import java.time.Instant;

public record UserLibraryItemResponse(
        long id,
        String name,
        String category,
        String description,
        String shapeType,
        String colorHex,
        double defaultScaleX,
        double defaultScaleY,
        double defaultScaleZ,
        boolean publicForImport,
        Long sourceItemId,
        String ownerLabel,
        Instant createdAt
) {}
