package com.landscape.design.repository;

import com.landscape.design.domain.User;
import com.landscape.design.domain.UserCatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCatalogItemRepository extends JpaRepository<UserCatalogItem, Long> {

    List<UserCatalogItem> findByOwnerOrderBySortOrderAscIdAsc(User owner);

    List<UserCatalogItem> findByPublicForImportIsTrueAndOwnerNotOrderByCreatedAtDesc(User excludeOwner);

    Optional<UserCatalogItem> findByIdAndOwner(Long id, User owner);

    @Query("SELECT u FROM UserCatalogItem u WHERE u.id = :id AND u.publicForImport = true")
    Optional<UserCatalogItem> findImportableById(@Param("id") Long id);
}
