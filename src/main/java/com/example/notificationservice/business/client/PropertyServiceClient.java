package com.example.notificationservice.business.client;

import com.example.notificationservice.domain.request.UpdatePropertyRentedRequest;
import com.example.notificationservice.domain.response.PropertyServiceResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;


//Property Service REST Client
// Communicates with Property Service to fetch property details
@HttpExchange
public interface PropertyServiceClient {
    @GetExchange("/api/v1/properties/{id}")
    PropertyServiceResponse getPropertyById(@PathVariable("id") Long id);

    @PutExchange("/api/v1/properties/{id}")
    PropertyServiceResponse updateProperty(@PathVariable("id") Long id,
                                           @RequestBody UpdatePropertyRentedRequest propertyRequest);

}




























