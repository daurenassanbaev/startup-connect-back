package com.daurenassanbaev.commentservice.service;

import com.daurenassanbaev.commentservice.db.dto.CommentDto;
import com.daurenassanbaev.commentservice.db.entity.Comment;
import com.daurenassanbaev.commentservice.db.repository.CommentRepository;
import com.daurenassanbaev.commentservice.exception.NotFoundException;
import com.daurenassanbaev.commentservice.mapper.CommentDtoMapper;
import com.daurenassanbaev.commentservice.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final CommentDtoMapper commentDtoMapper;
    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, Map<String, String>> kafkaTemplate;

    @Transactional
    public Optional<CommentDto> update(Integer id, CommentDto commentDto) {
        log.info("Attempting to update comment with id {}", id);
        var optional = commentRepository.findById(id);
        if (optional.isPresent()) {
            Comment comment = optional.get();
            log.debug("Found comment: {}", comment);
            comment.setUserId(commentDto.getUserId());
            comment.setIdeaId(commentDto.getIdeaId());
            comment.setContent(commentDto.getContent());
            Comment updatedComment = commentRepository.save(comment);
            log.info("Successfully updated comment with id {}", updatedComment.getId());
            commentDto.setId(updatedComment.getId());
            return Optional.of(commentDto);
        } else {
            log.warn("Comment with id {} not found", id);
            return Optional.empty();
        }
    }

    @Transactional
    public boolean delete(Integer id) {
        log.info("Attempting to delete comment with id {}", id);
        Optional<Comment> optional = commentRepository.findById(id);
        if (optional.isPresent()) {
            commentRepository.delete(optional.get());
            log.info("Successfully deleted comment with id {}", id);
            return true;
        } else {
            log.warn("Comment with id {} not found", id);
            return false;
        }
    }

    @Transactional
    public CommentDto create(CommentDto commentDto, String username, String token) {
        log.info("Attempting to create a new comment for ideaId {} by user {}", commentDto.getIdeaId(), username);
        var res = commentRepository.save(commentMapper.map(commentDto));
        commentDto.setId(res.getId());
        String comment = commentDto.getContent();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.debug("Fetching idea details for ideaId {}", commentDto.getIdeaId());
            ResponseEntity<IdeaResponse> response = restTemplate.exchange(
                    "http://localhost:8084/api/v1/ideas/" + commentDto.getIdeaId(),
                    HttpMethod.GET,
                    entity,
                    IdeaResponse.class);
            String title = response.getBody().getTitle();

            log.debug("Fetching user details for userId {}", response.getBody().getUserId());
            HttpEntity<Void> entity1 = new HttpEntity<>(headers);
            ResponseEntity<String> userResponse = restTemplate.exchange(
                    "http://localhost:8084/api/v1/users/" + response.getBody().getUserId(),
                    HttpMethod.GET,
                    entity1,
                    String.class
            );
            String toEmail = userResponse.getBody();

            String message = String.format("We are pleased to inform you that %s has left a comment on your post %s.\n Comment: \n %s", username, title, comment);
            Map<String, String> map = new HashMap<>();
            map.put("email", toEmail);
            map.put("message", message);
            map.put("subject", "New comment on your idea!");

            kafkaTemplate.send("notification-topic", map);
            log.info("Sent notification email to {}", toEmail);
            return commentDto;
        } catch (RestClientException e) {
            log.error("Unable to retrieve idea or user information for comment creation", e);
            throw new NotFoundException("Unable to retrieve idea or user information");
        }
    }

    public List<CommentDto> findAll(Integer ideaId) {
        log.info("Fetching all comments for ideaId {}", ideaId);
        List<Comment> list = commentRepository.findAllByIdeaId(ideaId);
        log.debug("Found {} comments for ideaId {}", list.size(), ideaId);
        return list.stream().map(commentDtoMapper::map).toList();
    }
}
