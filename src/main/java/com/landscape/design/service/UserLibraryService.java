package com.landscape.design.service;

import com.landscape.design.domain.User;
import com.landscape.design.domain.UserCatalogItem;
import com.landscape.design.dto.UserLibraryItemPatch;
import com.landscape.design.dto.UserLibraryItemRequest;
import com.landscape.design.dto.UserLibraryItemResponse;
import com.landscape.design.repository.UserCatalogItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLibraryService {

    private final UserCatalogItemRepository repository;

    @Transactional(readOnly = true)
    public List<UserLibraryItemResponse> listMine(User owner) {
        return repository.findByOwnerOrderBySortOrderAscIdAsc(owner).stream()
                .map(i -> toResponse(i, ownerLabel(i.getOwner())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserLibraryItemResponse> listBrowseable(User currentUser) {
        return repository.findByPublicForImportIsTrueAndOwnerNotOrderByCreatedAtDesc(currentUser).stream()
                .map(i -> toResponse(i, ownerLabel(i.getOwner())))
                .toList();
    }

    @Transactional
    public UserLibraryItemResponse create(User owner, UserLibraryItemRequest req) {
        UserCatalogItem item = new UserCatalogItem();
        item.setOwner(owner);
        applyRequest(item, req);
        item.setSortOrder(0);
        item.setSourceItem(null);
        repository.save(item);
        return toResponse(item, ownerLabel(owner));
    }

    @Transactional
    public UserLibraryItemResponse update(User owner, long id, UserLibraryItemPatch patch) {
        UserCatalogItem item = repository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Объект не найден"));
        if (patch.getPublicForImport() != null) {
            item.setPublicForImport(patch.getPublicForImport());
        }
        repository.save(item);
        return toResponse(item, ownerLabel(owner));
    }

    @Transactional
    public void delete(User owner, long id) {
        UserCatalogItem item = repository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Объект не найден"));
        repository.delete(item);
    }

    @Transactional
    public UserLibraryItemResponse importCopy(User currentUser, long sourceId) {
        UserCatalogItem source = repository.findImportableById(sourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Объект недоступен для импорта"));
        if (source.getOwner().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Это уже ваш объект");
        }
        UserCatalogItem copy = new UserCatalogItem();
        copy.setOwner(currentUser);
        copy.setName(source.getName());
        copy.setCategory(source.getCategory());
        copy.setDescription(source.getDescription());
        copy.setShapeType(source.getShapeType());
        copy.setColorHex(source.getColorHex());
        copy.setDefaultScaleX(source.getDefaultScaleX());
        copy.setDefaultScaleY(source.getDefaultScaleY());
        copy.setDefaultScaleZ(source.getDefaultScaleZ());
        copy.setSortOrder(0);
        copy.setPublicForImport(false);
        copy.setSourceItem(source);
        repository.save(copy);
        return toResponse(copy, ownerLabel(currentUser));
    }

    private static void applyRequest(UserCatalogItem item, UserLibraryItemRequest req) {
        item.setName(req.getName().trim());
        item.setCategory(req.getCategory().trim());
        item.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        item.setShapeType(req.getShapeType().trim());
        item.setColorHex(req.getColorHex() != null && !req.getColorHex().isBlank() ? req.getColorHex().trim() : null);
        item.setDefaultScaleX(req.getDefaultScaleX());
        item.setDefaultScaleY(req.getDefaultScaleY());
        item.setDefaultScaleZ(req.getDefaultScaleZ());
        item.setPublicForImport(req.isPublicForImport());
    }

    private static UserLibraryItemResponse toResponse(UserCatalogItem i, String ownerLabel) {
        return new UserLibraryItemResponse(
                i.getId(),
                i.getName(),
                i.getCategory(),
                i.getDescription(),
                i.getShapeType(),
                i.getColorHex(),
                i.getDefaultScaleX(),
                i.getDefaultScaleY(),
                i.getDefaultScaleZ(),
                i.isPublicForImport(),
                i.getSourceItem() != null ? i.getSourceItem().getId() : null,
                ownerLabel,
                i.getCreatedAt()
        );
    }

    private static String ownerLabel(User u) {
        if (u.getDisplayName() != null && !u.getDisplayName().isBlank()) {
            return u.getDisplayName().trim();
        }
        String email = u.getEmail();
        int at = email.indexOf('@');
        if (at <= 1) {
            return email;
        }
        return email.charAt(0) + "***@" + email.substring(at + 1);
    }
}
