package com.landscape.design.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProjectRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 2000)
    private String description;
}
