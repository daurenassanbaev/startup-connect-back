package com.daurenassanbaev.userservice.service.keycloak;

import com.daurenassanbaev.userservice.db.entity.Photo;
import com.daurenassanbaev.userservice.db.repository.PhotoRepository;
import com.daurenassanbaev.userservice.dto.LoginDto;
import com.daurenassanbaev.userservice.dto.UserDto;
import com.daurenassanbaev.userservice.dto.UserShowDto;
import com.daurenassanbaev.userservice.exceptions.InvalidImageFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class KeycloakUserServiceImpl implements KeycloakUserService {

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client}")
    private String client;

    @Value("${keycloak.clientSecret}")
    private String clientSecret;

    private final RestTemplate restTemplate;
    private final PhotoRepository photoRepository;
    private final Keycloak keycloak;

    public KeycloakUserServiceImpl(Keycloak keycloak, RestTemplate restTemplate, PhotoRepository photoRepository) {
        this.keycloak = keycloak;
        this.restTemplate = restTemplate;
        this.photoRepository = photoRepository;
    }

    @Override
    public UserDto createUser(UserDto userDto, MultipartFile file) {
        log.info("Attempting to create user: {}", userDto.getUsername());

        String contentType = file.getContentType();
        if (!(contentType != null && (contentType.equals(MediaType.IMAGE_PNG_VALUE)
                || contentType.equals(MediaType.IMAGE_JPEG_VALUE)
                || contentType.equals(MediaType.IMAGE_GIF_VALUE)))) {
            log.error("Invalid image content type: {}", contentType);
            throw new InvalidImageFileException("Unsupported content type: " + contentType + ". Only images and GIF files are supported.");
        }

        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setEmailVerified(true);
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setValue(userDto.getPassword());
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(false);
        List<CredentialRepresentation> list = new ArrayList<>();
        list.add(credential);
        user.setCredentials(list);

        UsersResource userResource = getUsersResource();
        var response = userResource.create(user);

        if (Objects.equals(response.getStatus(), 201)) {
            String userId = response.getLocation().getPath().split("/")[response.getLocation().getPath().split("/").length - 1];
            log.info("User created successfully with ID: {}", userId);

            RolesResource rolesResource = keycloak.realm(realm).roles();
            RoleRepresentation role = rolesResource.get(userDto.getRole()).toRepresentation();
            UserResource createdUserResource = userResource.get(userId);
            createdUserResource.roles().realmLevel().add(List.of(role));

            if (file != null) {
                log.info("Uploading profile image for user: {}", userId);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", file.getResource());

                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<String> result = restTemplate.exchange(
                        "http://localhost:9090/api/v1/s3/upload",
                        HttpMethod.POST,
                        entity,
                        String.class
                );
                log.info("Image uploaded successfully. URL: {}", result.getBody());

                Photo photo = new Photo();
                photo.setUrl(result.getBody());
                photo.setUserId(userId);
                photoRepository.save(photo);
            }

            return userDto;
        } else {
            log.error("Failed to create user. Response status: {}", response.getStatus());
        }
        return null;
    }

    @Override
    public String getUserEmailById(String userId) {
        log.info("Fetching email for user ID: {}", userId);
        return getUsersResource().get(userId).toRepresentation().getEmail();
    }

    @Override
    public UserRepresentation getUserById(String userId) {
        log.info("Fetching user by ID: {}", userId);
        return getUsersResource().get(userId).toRepresentation();
    }

    @Override
    public void deleteUserById(String userId) {
        log.info("Deleting user with ID: {}", userId);
        getUsersResource().delete(userId);
    }

    public String login(LoginDto loginDto) {
        log.info("User attempting to login: {}", loginDto.getUsername());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(client, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", loginDto.getUsername());
        body.add("password", loginDto.getPassword());
        body.add("client_id", client);
        body.add("scope", "openid");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:8080/realms/sandbox/protocol/openid-connect/token",
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("User logged in successfully: {}", loginDto.getUsername());
            return response.getBody().get("access_token").toString();
        } else {
            log.error("Invalid login attempt for user: {}", loginDto.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    public UserShowDto findByUserId(String userId) {
        log.info("Fetching user details for user ID: {}", userId);
        var res = getUserById(userId);
        UserShowDto userShowDto = new UserShowDto();
        userShowDto.setEmail(res.getEmail());
        userShowDto.setUsername(res.getUsername());
        userShowDto.setLastName(res.getLastName());
        userShowDto.setFirstName(res.getFirstName());
        userShowDto.setUrl(photoRepository.findByUserId(userId).get().getUrl());
        return userShowDto;
    }

    public void deletePhoto(String filename, String userId) {
        log.info("Deleting photo with filename: {} for user ID: {}", filename, userId);

        restTemplate.exchange(
                "http://localhost:9090/api/v1/s3/" + filename,
                HttpMethod.DELETE,
                null,
                String.class
        );
        photoRepository.deleteByUserIdAndUrl(userId, filename);
        log.info("Photo deleted successfully.");
    }

    private UsersResource getUsersResource() {
        return keycloak.realm(realm).users();
    }
}