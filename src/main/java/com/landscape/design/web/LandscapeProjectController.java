package com.landscape.design.web;

import com.landscape.design.dto.CreateProjectRequest;
import com.landscape.design.dto.ProjectResponse;
import com.landscape.design.dto.UpdateSceneRequest;
import com.landscape.design.domain.User;
import com.landscape.design.security.LandscapeUserPrincipal;
import com.landscape.design.service.LandscapeProjectService;
import com.landscape.design.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class LandscapeProjectController {

    private final LandscapeProjectService projectService;
    private final UserRepository userRepository;

    @GetMapping
    public List<ProjectResponse> list(@AuthenticationPrincipal LandscapeUserPrincipal principal) {
        User user = loadUser(principal);
        return projectService.list(user);
    }

    @GetMapping("/{id}")
    public ProjectResponse get(
            @AuthenticationPrincipal LandscapeUserPrincipal principal,
            @PathVariable UUID id
    ) {
        return projectService.get(loadUser(principal), id);
    }

    @PostMapping
    public ProjectResponse create(
            @AuthenticationPrincipal LandscapeUserPrincipal principal,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return projectService.create(loadUser(principal), request);
    }

    @PutMapping("/{id}/scene")
    public ProjectResponse updateScene(
            @AuthenticationPrincipal LandscapeUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSceneRequest request
    ) {
        return projectService.updateScene(loadUser(principal), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @AuthenticationPrincipal LandscapeUserPrincipal principal,
            @PathVariable UUID id
    ) {
        projectService.delete(loadUser(principal), id);
    }

    private User loadUser(LandscapeUserPrincipal principal) {
        return userRepository.findById(principal.getId()).orElseThrow();
    }
}
