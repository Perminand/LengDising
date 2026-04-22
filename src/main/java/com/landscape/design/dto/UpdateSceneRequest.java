package com.landscape.design.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSceneRequest {

    @NotNull
    private String sceneState;
}
