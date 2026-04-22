package com.landscape.design.web;

import com.landscape.design.domain.User;
import com.landscape.design.dto.UserLibraryItemPatch;
import com.landscape.design.dto.UserLibraryItemRequest;
import com.landscape.design.dto.UserLibraryItemResponse;
import com.landscape.design.security.LandscapeUserPrincipal;
import com.landscape.design.service.UserLibraryService;
import com.landscape.design.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserLibraryController {

    private final UserLibraryService userLibraryService;
    private final UserRepository userRepository;

    @GetMapping("/api/my-library/items")
    public List<UserLibraryItemResponse> myItems(@AuthenticationPrincipal LandscapeUserPrincipal principal) {
        return userLibraryService.listMine(loadUser(principal));
    }

    @PostMapping("/api/my-library/items")
    public UserLibraryItemResponse create(
            @AuthenticationPrincipal LandscapeUserPrincipal principal,
            @Valid @RequestBody UserLibraryItemRequest request
    ) {
        return userLibraryService.create(loadUser(principal), request);
    }

    @PatchMapping("/api/my-library/items/{id}")
    public UserLibraryItemResponse patch(
            @AuthenticationPrincipal LandscapeUserPrincipal principal,
            @PathVariable long id,
            @RequestBody UserLibraryItemPatch patch
    ) {
        return userLibraryService.update(loadUser(principal), id, patch);
    }

    @DeleteMapping("/api/my-library/items/{id}")
    public void delete(
            @AuthenticationPrincipal LandscapeUserPrincipal principal,
            @PathVariable long id
    ) {
        userLibraryService.delete(loadUser(principal), id);
    }

    @GetMapping("/api/library/browse")
    public List<UserLibraryItemResponse> browse(@AuthenticationPrincipal LandscapeUserPrincipal principal) {
        return userLibraryService.listBrowseable(loadUser(principal));
    }

    @PostMapping("/api/my-library/items/import/{sourceId}")
    public UserLibraryItemResponse importItem(
            @AuthenticationPrincipal LandscapeUserPrincipal principal,
            @PathVariable long sourceId
    ) {
        return userLibraryService.importCopy(loadUser(principal), sourceId);
    }

    private User loadUser(LandscapeUserPrincipal principal) {
        return userRepository.findById(principal.getId()).orElseThrow();
    }
}
