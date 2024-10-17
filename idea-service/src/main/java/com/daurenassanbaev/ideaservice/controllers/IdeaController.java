package com.daurenassanbaev.ideaservice.controllers;

import com.daurenassanbaev.ideaservice.controllers.dto.*;
import com.daurenassanbaev.ideaservice.database.dto.IdeaDto;
import com.daurenassanbaev.ideaservice.database.dto.IdeaShowDto;
import com.daurenassanbaev.ideaservice.service.IdeaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ideas")
@RequiredArgsConstructor
public class IdeaController {
    private final IdeaService ideaService;
    private final Validator validator ;
    @PostMapping
    @PreAuthorize("hasRole('ROLE_STARTUPER')")
    public ResponseEntity<?> createIdea(@RequestParam("idea") String ideaJson, @RequestParam(value = "file", required = false) MultipartFile file, @AuthenticationPrincipal Jwt jwt) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        IdeaDto ideaDto = objectMapper.readValue(ideaJson, IdeaDto.class);
        BindingResult bindingResult = new BeanPropertyBindingResult(ideaDto, "user");
        validator.validate(ideaDto, bindingResult);
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        ideaService.save(ideaDto, jwt.getSubject(), file);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("{id}/{filename}")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") Integer id, @PathVariable("filename") String filename, @AuthenticationPrincipal Jwt jwt) throws JsonProcessingException {
        ideaService.delete(id, jwt.getSubject(), filename);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PostMapping("/{id}/ratings")
    @PreAuthorize("hasRole('ROLE_INVESTOR')")
    public ResponseEntity<RatingDto> addRating(@PathVariable("id") Integer ideaId, @RequestParam Integer score, @AuthenticationPrincipal Jwt jwt) {
        RatingDto ratingDto = new RatingDto();
        ratingDto.setIdeaId(ideaId);
        ratingDto.setUserId(jwt.getSubject());
        ratingDto.setScore(score);
        ideaService.addRating(ratingDto, jwt.getTokenValue());
        return new ResponseEntity<>(ratingDto, HttpStatus.CREATED);
    }
    @GetMapping("/{id}/ratings")
    public ResponseEntity<List<RatingDto>> findAllRatings(@PathVariable("id") Integer ideaId, @AuthenticationPrincipal Jwt jwt) {
        List<RatingDto> list = ideaService.findAllRatings(ideaId, jwt.getTokenValue());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable("id") Integer ideaId, @Valid @RequestBody CommentRequest commentRequest, BindingResult bindingResult,@AuthenticationPrincipal Jwt jwt) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        CommentDto commentDto = new CommentDto();
        commentDto.setIdeaId(ideaId);
        commentDto.setUserId(jwt.getSubject());
        commentDto.setContent(commentRequest.getContent());
        ideaService.addComment(commentDto, jwt.getTokenValue());
        return new ResponseEntity<>(commentDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> findAllComments(@PathVariable("id") Integer ideaId, @AuthenticationPrincipal Jwt jwt) {
        List<CommentDto> list = ideaService.findAllComments(ideaId, jwt.getTokenValue());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<IdeaResponse> findById(@PathVariable("id") Integer ideaId) {
        IdeaResponse response = ideaService.findById(ideaId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/find/{id}")
    public ResponseEntity<IdeaShowDto> findByIdWithFile(@PathVariable("id") Integer id, @AuthenticationPrincipal Jwt jwt) {
        var res = ideaService.findByIdWithFile(id, jwt.getSubject());
        return ResponseEntity.ok(res);
    }
    @DeleteMapping("/file/{filename}")
    public void deletePhoto(@PathVariable("filename") String fileName, @AuthenticationPrincipal Jwt jwt) {
        ideaService.deleteFile(fileName, jwt.getSubject());
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_INVESTOR')")
    public ResponseEntity<?> updateRating(@RequestBody RatingRequest ratingRequest, BindingResult bindingResult, @AuthenticationPrincipal Jwt jwt) {
        ideaService.updateRating(ratingRequest);
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
