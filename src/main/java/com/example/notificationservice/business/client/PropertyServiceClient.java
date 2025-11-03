package com.example.notificationservice.clients;

import com.example.notificationservice.domain.response.PropertyServiceResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;



//Property Service REST Client
// Communicates with Property Service to fetch property details
@HttpExchange
public interface PropertyServiceClient {
    @GetExchange("/api/v1/properties/{id}")
    PropertyServiceResponse getPropertyById(@PathVariable("id") Long id);
}




























