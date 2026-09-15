package com.andesstay.msbff.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class UnitRequest {
  private String name;
  private String description;
  private String address;
  private UnitType type;
  private String city;
  private Boolean availability;
  private Integer rooms;
  private Integer bathrooms;
  private BigDecimal pricePerNight;
  private Integer maxOccupancy;
} 