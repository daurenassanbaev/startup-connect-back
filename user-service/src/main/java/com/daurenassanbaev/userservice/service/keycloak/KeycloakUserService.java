package com.daurenassanbaev.userservice.service.keycloak;

import com.daurenassanbaev.userservice.dto.LoginDto;
import com.daurenassanbaev.userservice.dto.UserDto;
import com.daurenassanbaev.userservice.dto.UserShowDto;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.web.multipart.MultipartFile;

public interface KeycloakUserService {

    UserDto createUser(UserDto userDto, MultipartFile file);
    UserRepresentation getUserById(String userId);
    String getUserEmailById(String userId);
    void deleteUserById(String userId);
    String login(LoginDto loginDto);
    UserShowDto findByUserId(String userId);
}
