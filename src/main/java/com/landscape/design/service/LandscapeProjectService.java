package com.landscape.design.service;

import com.landscape.design.config.SubscriptionProperties;
import com.landscape.design.domain.LandscapeProject;
import com.landscape.design.domain.User;
import com.landscape.design.dto.CreateProjectRequest;
import com.landscape.design.dto.ProjectResponse;
import com.landscape.design.dto.UpdateSceneRequest;
import com.landscape.design.repository.LandscapeProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LandscapeProjectService {

    private final LandscapeProjectRepository projectRepository;
    private final SubscriptionService subscriptionService;
    private final SceneStateService sceneStateService;

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(User owner) {
        return projectRepository.findByOwnerOrderByUpdatedAtDesc(owner).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(User owner, UUID id) {
        LandscapeProject p = projectRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));
        return toResponse(p);
    }

    @Transactional
    public ProjectResponse create(User owner, CreateProjectRequest req) {
        SubscriptionProperties.TierLimits limits = subscriptionService.limitsFor(owner);
        long count = projectRepository.countByOwner(owner);
        if (count >= limits.getMaxProjects()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Лимит проектов для вашего тарифа: " + limits.getMaxProjects());
        }
        LandscapeProject p = new LandscapeProject();
        p.setId(UUID.randomUUID());
        p.setOwner(owner);
        p.setName(req.getName().trim());
        p.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        p.setSceneState(sceneStateService.emptyScene());
        projectRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public ProjectResponse updateScene(User owner, UUID id, UpdateSceneRequest req) {
        LandscapeProject p = projectRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));
        sceneStateService.validateSceneJson(req.getSceneState());
        int n = sceneStateService.countObjects(req.getSceneState());
        SubscriptionProperties.TierLimits limits = subscriptionService.limitsFor(owner);
        if (n > limits.getMaxSceneElements()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Лимит объектов на сцене для вашего тарифа: " + limits.getMaxSceneElements());
        }
        p.setSceneState(req.getSceneState());
        projectRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public void delete(User owner, UUID id) {
        LandscapeProject p = projectRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Проект не найден"));
        projectRepository.delete(p);
    }

    private ProjectResponse toResponse(LandscapeProject p) {
        return new ProjectResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getSceneState(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
