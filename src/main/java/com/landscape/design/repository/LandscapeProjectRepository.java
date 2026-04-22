package com.landscape.design.repository;

import com.landscape.design.domain.LandscapeProject;
import com.landscape.design.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LandscapeProjectRepository extends JpaRepository<LandscapeProject, UUID> {

    List<LandscapeProject> findByOwnerOrderByUpdatedAtDesc(User owner);

    long countByOwner(User owner);

    Optional<LandscapeProject> findByIdAndOwner(UUID id, User owner);
}
