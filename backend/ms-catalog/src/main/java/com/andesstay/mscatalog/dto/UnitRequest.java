package com.andesstay.mscatalog.dto;

import com.andesstay.mscatalog.model.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class UnitRequest {
    @NotBlank private String name;
    @NotBlank private String description;
    @NotBlank private String address;
    @NotNull private UnitType type;
    @NotBlank private String city;
    @NotNull private Boolean availability;
    @NotNull @Min(1) private Integer rooms;
    @NotNull @Min(1) private Integer bathrooms;
    @NotNull @DecimalMin("0.01") private BigDecimal pricePerNight;
    @NotNull @Min(1) private Integer maxOccupancy;

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

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setAddress(String address) { this.address = address; }
    public void setType(UnitType type) { this.type = type; }
    public void setCity(String city) { this.city = city; }
    public void setAvailability(Boolean availability) { this.availability = availability; }
    public void setRooms(Integer rooms) { this.rooms = rooms; }
    public void setBathrooms(Integer bathrooms) { this.bathrooms = bathrooms; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    public void setMaxOccupancy(Integer maxOccupancy) { this.maxOccupancy = maxOccupancy; }
}
