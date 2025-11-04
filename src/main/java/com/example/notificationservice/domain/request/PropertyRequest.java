package com.example.notificationservice.domain.request;


import com.example.notificationservice.persistence.model.HouseType;
import com.example.notificationservice.persistence.model.LocationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequest {

    @NotBlank(message = "Property title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @Enumerated(EnumType.STRING)
    private HouseType propertyType;

    @NotNull(message = "Quantity is required")
    private Integer quantity;
    @Enumerated(EnumType.STRING)
    private LocationType locationType;
    @NotNull(message = "Rent amount is required")
    @DecimalMin(value = "100.0", message = "Rent must be at least €100")
    @DecimalMax(value = "5000.0", message = "Rent cannot exceed €5000")
    private BigDecimal rentAmount;

    @NotNull(message = "Security deposit is required")
    @DecimalMin(value = "0.0", message = "Security deposit cannot be negative")
    private BigDecimal securityDeposit;

    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @NotBlank(message = "Rental condition is required")
    private String rentalcondition;
    @NotBlank(message = "Surface area is required")
    private String  surfaceArea;

    @NotNull(message = "Property rental status is required")
    @JsonProperty("propertyIsRented")
   private boolean propertyIsRented;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "\\d{4}[A-Z]{2}", message = "Invalid Dutch postal code format")
    private String postalCode;
@NotBlank(message = "Available date is required")
    private String interior;
    @NotBlank(message = "Available date is required")
    private String availableDate;


    @Min(value = 1, message = "Number of bedrooms must be at least 1")
    @Max(value = 10, message = "Number of bedrooms cannot exceed 10")
    private Integer bedrooms;

    @NotBlank(message = "Image is required")
    private String image;
    @NotBlank(message = "Image1 is required")
    private String image2;
    @NotBlank(message = "Image2 is required")
    private String image3;
    @NotBlank(message = "Image3 is required")
    private String image4;
}
