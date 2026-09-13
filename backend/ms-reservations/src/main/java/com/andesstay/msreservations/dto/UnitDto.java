package com.andesstay.msreservations.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UnitDto {
    private Long unitId;
    private String name;
    private String description;
    private String address;
    private String city;
    private Boolean availability;
    private Integer rooms;
    private Integer bathrooms;
    private BigDecimal pricePerNight;
    private Integer maxOccupancy;
}
