package com.landscape.design.service;

import com.landscape.design.domain.CatalogDetail;
import com.landscape.design.dto.CatalogDetailResponse;
import com.landscape.design.repository.CatalogDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CatalogDetailRepository catalogDetailRepository;

    @Transactional(readOnly = true)
    public List<CatalogDetailResponse> listAll() {
        return catalogDetailRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(this::toDto)
                .toList();
    }

    private CatalogDetailResponse toDto(CatalogDetail d) {
        return new CatalogDetailResponse(
                d.getId(),
                d.getName(),
                d.getCategory(),
                d.getDescription(),
                d.getShapeType(),
                d.getColorHex(),
                d.getDefaultScaleX(),
                d.getDefaultScaleY(),
                d.getDefaultScaleZ()
        );
    }
}
