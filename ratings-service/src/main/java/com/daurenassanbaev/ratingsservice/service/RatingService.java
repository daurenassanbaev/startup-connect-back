package com.daurenassanbaev.ratingsservice.service;

import com.daurenassanbaev.ratingsservice.database.dto.RatingDto;
import com.daurenassanbaev.ratingsservice.database.entity.Rating;
import com.daurenassanbaev.ratingsservice.database.repository.RatingsRepository;
import com.daurenassanbaev.ratingsservice.exceptions.RatingAlreadyExistsException;
import com.daurenassanbaev.ratingsservice.mapper.RatingDtoMapper;
import com.daurenassanbaev.ratingsservice.mapper.RatingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RatingService {

    private final RatingsRepository ratingsRepository;
    private final RatingMapper ratingMapper;
    private final RatingDtoMapper ratingDtoMapper;
    private final KafkaTemplate<String, Map<String, String>> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Transactional
    public Optional<RatingDto> update(Integer id, RatingDto ratingDto) {
        log.info("Updating rating with id {}", id);
        var optional = ratingsRepository.findById(id);
        if (optional.isPresent()) {
            log.info("Rating found for id {}", id);
            Rating rating = optional.get();
            rating.setScore(ratingDto.getScore());
            rating.setUserId(ratingDto.getUserId());
            rating.setIdeaId(ratingDto.getIdeaId());
            Rating updatedRating = ratingsRepository.save(rating);
            ratingDto.setId(updatedRating.getId());
            log.info("Rating updated successfully for id {}", id);
            return Optional.of(ratingDto);
        } else {
            log.warn("No rating found for id {}", id);
            return Optional.empty();
        }
    }

    @Transactional
    public boolean delete(Integer id, Integer ideaId, String token) {
        log.info("Deleting rating with id {}", id);
        Optional<Rating> optional = ratingsRepository.findById(id);
        if (optional.isPresent()) {
            ratingsRepository.delete(optional.get());
            log.info("Rating deleted for id {}", id);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            RatingResponse rating = new RatingResponse();
            Double avg = ratingsRepository.calculateAverageScore(ideaId);
            rating.setRating(avg);
            rating.setIdeaId(ideaId);
            HttpEntity<RatingResponse> entity2 = new HttpEntity<>(rating, headers);

            restTemplate.exchange(
                    "http://IDEA-SERVICE:8084/api/v1/ideas",
                    HttpMethod.PUT,
                    entity2,
                    RatingResponse.class
            );
            log.info("Updated average rating for ideaId {}", ideaId);
            return true;
        } else {
            log.warn("No rating found for id {}", id);
            return false;
        }
    }

    @Transactional
    public RatingDto create(RatingDto ratingDto, String username, String token) {
        log.info("Creating new rating for ideaId {} and userId {}", ratingDto.getIdeaId(), ratingDto.getUserId());
        Optional<Rating> result = ratingsRepository.findByIdeaIdAndUserId(ratingDto.getIdeaId(), ratingDto.getUserId());
        if (result.isPresent()) {
            log.warn("Rating already exists for ideaId {} and userId {}", ratingDto.getIdeaId(), ratingDto.getUserId());
            throw new RatingAlreadyExistsException("This rating for the idea already exists");
        } else {
            var res = ratingsRepository.save(ratingMapper.map(ratingDto));
            log.info("Rating saved successfully with id {}", res.getId());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            RatingResponse rating = new RatingResponse();
            Double avg = ratingsRepository.calculateAverageScore(ratingDto.getIdeaId());
            rating.setRating(avg);
            rating.setIdeaId(ratingDto.getIdeaId());
            HttpEntity<RatingResponse> entity2 = new HttpEntity<>(rating, headers);
            ratingDto.setId(res.getId());

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<IdeaResponse> response = restTemplate.exchange(
                    "http://IDEA-SERVICE/api/v1/ideas/"+ratingDto.getIdeaId(),
                    HttpMethod.GET,
                    entity,
                    IdeaResponse.class);
            String title = response.getBody().getTitle();

            HttpEntity<Void> entity1 = new HttpEntity<>(headers);
            var userId = response.getBody().getUserId();
            ResponseEntity<String> userResponse = restTemplate.exchange(
                    "http://USER-SERVICE/api/v1/users/"+userId,
                    HttpMethod.GET,
                    entity1,
                    String.class
            );

            restTemplate.exchange(
                    "http://IDEA-SERVICE/api/v1/ideas",
                    HttpMethod.PUT,
                    entity2,
                    RatingResponse.class
            );
            String toEmail = userResponse.getBody();

            String message = String.format("You received a rating of '%s' from investor '%s' for the idea '%s'",
                    ratingDto.getScore(),
                    username,
                    title);
            Map<String, String> map = new HashMap<>();

            map.put("email", toEmail);
            map.put("message", message);
            map.put("subject", "Congratulations! Your idea has been appreciated by an investor!");

            kafkaTemplate.send("notification-topic", map);

            log.info("Notification sent to {} for ideaId {}", toEmail, ratingDto.getIdeaId());
            return ratingDto;
        }
    }

    public List<RatingDto> findAll(Integer ideaId) {
        log.info("Fetching all ratings for ideaId {}", ideaId);
        List<Rating> list = ratingsRepository.findAllByIdeaId(ideaId);
        log.info("Found {} ratings for ideaId {}", list.size(), ideaId);
        return list.stream().map(ratingDtoMapper::map).toList();
    }
}
