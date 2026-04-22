package com.landscape.design.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "catalog_details")
@Getter
@Setter
@NoArgsConstructor
public class CatalogDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 1000)
    private String description;

    @Column(name = "shape_type", nullable = false, length = 40)
    private String shapeType;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(name = "default_scale_x", nullable = false)
    private double defaultScaleX = 1;

    @Column(name = "default_scale_y", nullable = false)
    private double defaultScaleY = 1;

    @Column(name = "default_scale_z", nullable = false)
    private double defaultScaleZ = 1;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
