package com.daurenassanbaev.ideaservice.service;

import com.daurenassanbaev.ideaservice.controllers.dto.CommentDto;
import com.daurenassanbaev.ideaservice.controllers.dto.IdeaResponse;
import com.daurenassanbaev.ideaservice.controllers.dto.RatingDto;
import com.daurenassanbaev.ideaservice.controllers.dto.RatingRequest;
import com.daurenassanbaev.ideaservice.database.dto.IdeaDto;
import com.daurenassanbaev.ideaservice.database.dto.IdeaShowDto;
import com.daurenassanbaev.ideaservice.database.entity.Idea;
import com.daurenassanbaev.ideaservice.database.entity.IdeaFile;
import com.daurenassanbaev.ideaservice.database.repository.IdeaFileRepository;
import com.daurenassanbaev.ideaservice.database.repository.IdeaRepository;
import com.daurenassanbaev.ideaservice.exception.FileUploadException;
import com.daurenassanbaev.ideaservice.exception.NotFoundException;
import com.daurenassanbaev.ideaservice.mapper.IdeaDtoMapper;
import com.daurenassanbaev.ideaservice.mapper.IdeaMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdeaServiceImpl implements IdeaService{
    private final IdeaRepository ideaRepository;
    private final IdeaDtoMapper ideaDtoMapper;
    private final IdeaMapper ideaMapper;
    private final RestTemplate restTemplate;
    private final IdeaFileRepository ideaFileRepository;

    @Override
    public void delete(Integer id, String userId, String url) {
        log.info("Deleting idea with id: {}", id);
        ideaRepository.deleteById(id);
        deleteFile(url, userId);
        log.info("Idea with id: {} deleted", id);

    }

    @Override
    public void deleteFile(String filename, String userId) {
        log.info("Deleting file {} for user {}", filename, userId);
        try {
            restTemplate.exchange(
                    "http://AMAZON-S3-SERVICE/api/v1/s3/" + filename,
                    HttpMethod.DELETE,
                    null,
                    String.class
            );
            ideaFileRepository.deleteByUserIdAndUrl(userId, filename);
            log.info("File {} for user {} deleted successfully", filename, userId);
        } catch (Exception e) {
            log.error("Error deleting file {}: {}", filename, e.getMessage());
        }
    }

    @SneakyThrows
    @Override
    public IdeaDto save(IdeaDto idea, String userId, MultipartFile file) {
        log.info("Saving idea for user {}", userId);
        ideaRepository.save(ideaMapper.map(idea, userId));
        if (file != null) {
            log.info("Uploading file for user {}", userId);
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", file.getResource());

                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<String> result = restTemplate.exchange(
                        "http://AMAZON-S3-SERVICE/api/v1/s3/upload",
                        HttpMethod.POST,
                        entity,
                        String.class
                );
                IdeaFile ideaFile = new IdeaFile();
                ideaFile.setUrl(result.getBody());
                ideaFile.setUserId(userId);
                ideaFileRepository.save(ideaFile);
                log.info("File uploaded successfully for user {}", userId);
            } catch (Exception e) {
                log.error("File upload failed for user {}: {}", userId, e.getMessage());
                throw new FileUploadException("Failed to upload file", e);
            }
        }
        return idea;
    }

    @Override
    public List<IdeaDto> findAll() {
        log.info("Retrieving all ideas");
        List<IdeaDto> ideas = ideaRepository.findAll().stream().map(ideaDtoMapper::map).toList();
        log.info("Found {} ideas", ideas.size());
        return ideas;
    }

    @Override
    @SneakyThrows
    public IdeaResponse findById(Integer id) {
        log.info("Finding idea with id: {}", id);
        Optional<Idea> maybeIdea = ideaRepository.findById(id);
        if (maybeIdea.isPresent()) {
            IdeaResponse ideaResponse = new IdeaResponse();
            ideaResponse.setTitle(maybeIdea.get().getTitle());
            ideaResponse.setUserId(maybeIdea.get().getUserId());
            log.info("Idea with id: {} found", id);
            return ideaResponse;
        } else {
            log.error("Idea with id: {} not found", id);
            throw new NotFoundException("Idea with id : " + id + " not found");
        }
    }

    @Override
    public IdeaShowDto findByIdWithFile(Integer id, String userId) {
        log.info("Finding idea with file for id: {} and user {}", id, userId);
        Optional<Idea> maybeIdea = ideaRepository.findById(id);
        if (maybeIdea.isPresent()) {
            IdeaShowDto ideaResponse = new IdeaShowDto();
            ideaResponse.setTitle(maybeIdea.get().getTitle());
            ideaResponse.setDescription(maybeIdea.get().getUserId());
            ideaResponse.setUrl(ideaFileRepository.findByUserId(userId).get().getUrl());
            log.info("Idea with id: {} and file found for user {}", id, userId);
            return ideaResponse;
        } else {
            log.error("Idea with id: {} not found", id);
            throw new NotFoundException("Idea with id : " + id + " not found");
        }
    }

    @Override
    @Transactional
    public void addRating(RatingDto ratingDto, String token) {
        log.info("Adding rating for ideaId: {}", ratingDto.getIdeaId());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<RatingDto> entity = new HttpEntity<>(ratingDto, headers);

            restTemplate.postForObject(
                    "http://RATINGS-SERVICE/api/v1/ratings",
                    entity,
                    RatingDto.class
            );
            log.info("Rating added successfully for ideaId: {}", ratingDto.getIdeaId());
        } catch (Exception e) {
            log.error("Could not add rating for ideaId {}: {}", ratingDto.getIdeaId(), e.getMessage());
            throw new RestClientException("Could not add rating", e);
        }
    }

    @Override
    public List<RatingDto> findAllRatings(Integer ideaId, String token) {
        log.info("Finding all ratings for ideaId: {}", ideaId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<RatingDto>> response = restTemplate.exchange(
                    "http://RATINGS-SERVICE/api/v1/ratings/{id}",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<RatingDto>>() {},
                    ideaId
            );
            log.info("Found {} ratings for ideaId: {}", response.getBody().size(), ideaId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Could not find ratings for ideaId {}: {}", ideaId, e.getMessage());
            throw new RestClientException("Could not find ratings", e);
        }
    }

    @Override
    public void addComment(CommentDto commentDto, String tokenValue) {
        log.info("Adding comment for ideaId: {}", commentDto.getIdeaId());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokenValue);

            HttpEntity<CommentDto> entity = new HttpEntity<>(commentDto, headers);

            restTemplate.postForObject(
                    "http://COMMENT-SERVICE/api/v1/comments",
                    entity,
                    CommentDto.class
            );
            log.info("Comment added successfully for ideaId: {}", commentDto.getIdeaId());
        } catch (Exception e) {
            log.error("Could not add comment for ideaId {}: {}", commentDto.getIdeaId(), e.getMessage());
            throw new RestClientException("Could not add comment", e);
        }
    }

    @Override
    public List<CommentDto> findAllComments(Integer ideaId, String tokenValue) {
        log.info("Finding all comments for ideaId: {}", ideaId);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tokenValue);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<CommentDto>> response = restTemplate.exchange(
                    "http://COMMENT-SERVICE/api/v1/comments/{id}",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<CommentDto>>() {},
                    ideaId
            );
            log.info("Found {} comments for ideaId: {}", response.getBody().size(), ideaId);
            return response.getBody();
        } catch (Exception e) {
            log.error("Could not find comments for ideaId {}: {}", ideaId, e.getMessage());
            throw new RestClientException("Could not find comments", e);
        }
    }

    @Override
    public void updateRating(RatingRequest ratingRequest) {
        ideaRepository.updateById(ratingRequest.getIdeaId(), ratingRequest.getRating());
    }
}
