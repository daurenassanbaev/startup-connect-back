package com.daurenassanbaev.ratingsservice.contoller;

import com.daurenassanbaev.ratingsservice.database.dto.RatingDto;
import com.daurenassanbaev.ratingsservice.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
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
@RequestMapping("api/v1/ratings")
@RequiredArgsConstructor
public class RatingsController {
    private final RatingService ratingService;

    @PutMapping("{id}")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<?> update(@PathVariable("id") Integer id, @Valid @RequestBody RatingDto ratingDto, BindingResult bindingResult, @AuthenticationPrincipal Jwt jwt) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        ratingDto.setUserId(jwt.getSubject());
        var result = ratingService.update(id, ratingDto);
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(result.get(), HttpStatus.OK);
        }
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") Integer id, @RequestParam("ideaId") Integer ideaId, @AuthenticationPrincipal Jwt jwt) {
        boolean res = ratingService.delete(id, ideaId, jwt.getTokenValue());
        if (res) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<?> create(@Valid @RequestBody RatingDto ratingDto, BindingResult bindingResult, @AuthenticationPrincipal Jwt jwt) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(errors);
        }
        ratingDto.setUserId(jwt.getSubject());
        String username = jwt.getClaim("preferred_username");
        RatingDto result = ratingService.create(ratingDto, username, jwt.getTokenValue());
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<List<RatingDto>> findAll(@PathVariable("id") Integer ideaId, @AuthenticationPrincipal Jwt jwt) {
        List<RatingDto> list = ratingService.findAll(ideaId);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
