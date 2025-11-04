package com.example.notificationservice.domain.response;

import com.example.notificationservice.domain.dto.PropertyDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyServiceResponse {

    @JsonProperty("propertyId")
    private Long propertyId;

    private String message;
    private Boolean success;
    private String title;
    private String description;
    private String address; // This maps to streetAddress in request
    private String propertyType;
    private String locationType;
    private BigDecimal rentAmount;
    private BigDecimal securityDeposit;
    private String rentalcondition;
    private String surfaceArea;
    private String postalCode;
    private String interior;
    private String availableDate;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer quantity;
    private String condition;

    @JsonProperty("propertyIsRented")
    private Boolean propertyIsRented;

    private String image;
    private String image2;
    private String image3;
    private String image4;

    // Nested property object (might be null)
    private PropertyDto property;

    /**
     * Helper method to get property as PropertyDto
     */
    public PropertyDto getPropertyDto() {
        if (this.property != null) {
            return this.property;
        }

        return PropertyDto.builder()
                .id(this.propertyId)
                .title(this.title)
                .address(this.address)
                .propertyType(this.propertyType)
                .locationType(this.locationType)
                .rentAmount(this.rentAmount)
                .description(this.description)
                .surfaceArea(this.surfaceArea)
                .propertyIsRented(this.propertyIsRented)
                .bathrooms(this.bathrooms)
                .bedrooms(this.bedrooms)
                .quantity(this.quantity)
                .interior(this.interior)
                .condition(this.condition)
                .availableDate(this.availableDate)
                .postalCode(this.postalCode)
                .rentalcondition(this.rentalcondition)
                .isRented(this.propertyIsRented != null ? this.propertyIsRented : false)
                .securityDeposit(this.securityDeposit)
                .image(this.image)
                .image2(this.image2)
                .image3(this.image3)
                .image4(this.image4)
                .build();
    }
}