package com.example.notificationservice.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Property Data Transfer Object
 * Used for communication with Property Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDto {
    private Long id;
    private String title;
    private String address;
    private String propertyType;
    private String locationType;
    private BigDecimal rentAmount;
    private String description;
    private String surfaceArea;
    @NotNull(message = "Property rental status is required")
    @JsonProperty("propertyIsRented")
    private  Boolean propertyIsRented;
    private List<String> images;
    private Integer bathrooms;
    private Integer bedrooms;
    private Integer quantity;
    private String interior;
    private String condition;
    private String availableDate;
    private String postalCode;
    private String rentalcondition;
    private boolean isRented;
    private BigDecimal securityDeposit;
    private String image;
    private String image2;
    private String image3;
    private String image4;
}




























