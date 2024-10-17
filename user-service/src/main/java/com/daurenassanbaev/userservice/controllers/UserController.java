package com.daurenassanbaev.userservice.controllers;

import com.daurenassanbaev.userservice.dto.LoginDto;
import com.daurenassanbaev.userservice.dto.UserDto;
import com.daurenassanbaev.userservice.dto.UserShowDto;
import com.daurenassanbaev.userservice.service.keycloak.KeycloakUserServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final KeycloakUserServiceImpl keycloakUserService;
    private final Validator validator;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestParam("user") String userJson, @RequestParam(value = "file", required = false) MultipartFile file) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        UserDto userDto = objectMapper.readValue(userJson, UserDto.class);
        BindingResult bindingResult = new BeanPropertyBindingResult(userDto, "user");
        validator.validate(userDto, bindingResult);
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        return ResponseEntity.ok(keycloakUserService.createUser(userDto, file));
    }
    @GetMapping
    public UserRepresentation getUser(Principal principal) {
        return keycloakUserService.getUserById(principal.getName());
    }
    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable("userId") String userId) {
        keycloakUserService.deleteUserById(userId);
    }
    @DeleteMapping("/photo/{filename}")
    public void deletePhoto(@PathVariable("filename") String fileName, @AuthenticationPrincipal Jwt jwt) {
        keycloakUserService.deletePhoto(fileName, jwt.getSubject());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        String token = keycloakUserService.login(loginDto);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("{id}")
    public ResponseEntity<String> getUserEmailById(@PathVariable("id") String id) {
        return ResponseEntity.ok(keycloakUserService.getUserEmailById(id));
    }


    @GetMapping("/find/{id}")
    public ResponseEntity<UserShowDto> findUserById(@PathVariable("id") String id) {
        UserShowDto userShowDto = keycloakUserService.findByUserId(id);
        return ResponseEntity.ok(userShowDto);
    }

}
