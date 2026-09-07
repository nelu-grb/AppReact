package com.andesstay.mscatalog.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "UNITS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "unitId")
@ToString
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long unitId;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "ADDRESS", nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private UnitType type;

    @Column(name = "CITY", nullable = false)
    private String city;

    @Column(name = "AVAILABILITY")
    private Boolean availability;

    @Column(name = "ROOMS", nullable = false)
    private Integer rooms;

    @Column(name = "BATHROOMS", nullable = false)
    private Integer bathrooms;

    @Column(name = "PRICE_PER_NIGHT", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(name = "MAX_OCCUPANCY", nullable = false)
    private Integer maxOccupancy;
}