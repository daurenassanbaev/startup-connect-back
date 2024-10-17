package com.daurenassanbaev.commentservice.controller;

import com.daurenassanbaev.commentservice.db.dto.CommentDto;
import com.daurenassanbaev.commentservice.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    @PutMapping("{id}")
    public ResponseEntity<?> update(@PathVariable("id") Integer id, @Valid @RequestBody CommentDto commentDto, BindingResult bindingResult, @AuthenticationPrincipal Jwt jwt) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        commentDto.setUserId(jwt.getSubject());
        var result = commentService.update(id, commentDto);
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(result.get(), HttpStatus.OK);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") Integer id) {
        boolean res = commentService.delete(id);
        if (res) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CommentDto commentDto, BindingResult bindingResult,@AuthenticationPrincipal Jwt jwt) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        commentDto.setUserId(jwt.getSubject());
        CommentDto result = commentService.create(commentDto, jwt.getClaim("preferred_username"), jwt.getTokenValue());
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<List<CommentDto>> findAll(@PathVariable("id") Integer ideaId, @AuthenticationPrincipal Jwt jwt) {
        List<CommentDto> list = commentService.findAll(ideaId);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
