package com.landscape.design.dto;

public record CatalogDetailResponse(
        long id,
        String name,
        String category,
        String description,
        String shapeType,
        String colorHex,
        double defaultScaleX,
        double defaultScaleY,
        double defaultScaleZ
) {}
