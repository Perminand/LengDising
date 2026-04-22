package com.landscape.design.repository;

import com.landscape.design.domain.CatalogDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogDetailRepository extends JpaRepository<CatalogDetail, Long> {

    List<CatalogDetail> findAllByOrderBySortOrderAscIdAsc();
}
