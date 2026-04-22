package com.landscape.design.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLibraryItemRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Size(max = 80)
    private String category;

    @Size(max = 1000)
    private String description;

    @NotBlank
    @Size(max = 40)
    private String shapeType;

    @Size(max = 7)
    private String colorHex;

    private double defaultScaleX = 1;
    private double defaultScaleY = 1;
    private double defaultScaleZ = 1;

    private boolean publicForImport;
}
