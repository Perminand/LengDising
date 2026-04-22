package com.landscape.design.web;

import com.landscape.design.dto.CatalogDetailResponse;
import com.landscape.design.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/details")
    public List<CatalogDetailResponse> details() {
        return catalogService.listAll();
    }
}
