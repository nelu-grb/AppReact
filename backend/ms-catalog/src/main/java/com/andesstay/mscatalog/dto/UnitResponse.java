package com.andesstay.mscatalog.dto;

import com.andesstay.mscatalog.model.UnitType;

import java.math.BigDecimal;

public class UnitResponse {
    private Long unitId;
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

    public UnitResponse(Long unitId, String name, String description, String address, UnitType type,
                        String city, Boolean availability, Integer rooms, Integer bathrooms,
                        BigDecimal pricePerNight, Integer maxOccupancy) {
        this.unitId = unitId;
        this.name = name;
        this.description = description;
        this.address = address;
        this.type = type;
        this.city = city;
        this.availability = availability;
        this.rooms = rooms;
        this.bathrooms = bathrooms;
        this.pricePerNight = pricePerNight;
        this.maxOccupancy = maxOccupancy;
    }

    public Long getUnitId() { return unitId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public UnitType getType() { return type; }
    public String getCity() { return city; }
    public Boolean getAvailability() { return availability; }
    public Integer getRooms() { return rooms; }
    public Integer getBathrooms() { return bathrooms; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public Integer getMaxOccupancy() { return maxOccupancy; }
}
