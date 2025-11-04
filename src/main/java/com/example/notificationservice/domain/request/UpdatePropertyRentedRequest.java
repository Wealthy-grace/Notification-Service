package com.example.notificationservice.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UpdatePropertyRentedRequest {
    private String title;
    private String description;
    private String propertyType;
    private Integer quantity;
    private String locationType;
    private BigDecimal rentAmount;
    private BigDecimal securityDeposit;
    private String streetAddress;
    private String rentalcondition;
    private String surfaceArea;

    @JsonProperty("propertyIsRented")
    private Boolean propertyIsRented;

    private String postalCode;
    private String interior;
    private String availableDate;
    private Integer bedrooms;
    private String image;
    private String image2;
    private String image3;
    private String image4;
}
