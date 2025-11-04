package com.example.notificationservice.business.mapper;

import com.example.notificationservice.domain.dto.NotificationDto;
import com.example.notificationservice.domain.request.SendNotificationRequest;
import com.example.notificationservice.persistence.model.Notification;
import org.mapstruct.*;

import java.util.List;

/**
 * Notification Mapper
 * Maps between Notification entity and DTOs using MapStruct
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationMapper {

    /**
     * Convert Notification entity to DTO
     */
    NotificationDto toDto(Notification notification);

    /**
     * Convert list of Notification entities to DTOs
     */
    List<NotificationDto> toDtoList(List<Notification> notifications);

    /**
     * Convert DTO to Notification entity
     */
    Notification toEntity(NotificationDto dto);

    /**
     * Convert SendNotificationRequest to Notification entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "retryCount", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "htmlContent", ignore = true)
    Notification fromRequest(SendNotificationRequest request);

    /**
     * Update existing notification from DTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(NotificationDto dto, @MappingTarget Notification notification);
}




























