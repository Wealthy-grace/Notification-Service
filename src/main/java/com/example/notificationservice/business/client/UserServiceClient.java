package com.example.notificationservice.business.client;

import com.example.notificationservice.domain.dto.UserDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;


@HttpExchange
public interface UserServiceClient {


    @GetExchange("/api/internal/users/username/{username}")
    UserDto getUserByUsername(@PathVariable("username") String username);


    @GetExchange("/api/internal/users/id/{id}")
    UserDto getUserById(@PathVariable("id") Long id);


    @GetExchange("/api/internal/users/username/{username}/exists")
    Boolean userExistsByUsername(@PathVariable("username") String username);


    @GetExchange("/api/internal/users/username/{username}/role")
    String getUserRoleByUsername(@PathVariable("username") String username);


    // ✅ Update this path to match User Service
    @GetExchange("/api/auth/user/id/{id}")
    UserDto getUsersById(@PathVariable("id") Long id);
}




























