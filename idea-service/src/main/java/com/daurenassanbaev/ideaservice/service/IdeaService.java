package com.daurenassanbaev.ideaservice.service;

import com.daurenassanbaev.ideaservice.controllers.dto.CommentDto;
import com.daurenassanbaev.ideaservice.controllers.dto.IdeaResponse;
import com.daurenassanbaev.ideaservice.controllers.dto.RatingDto;
import com.daurenassanbaev.ideaservice.controllers.dto.RatingRequest;
import com.daurenassanbaev.ideaservice.database.dto.IdeaDto;
import com.daurenassanbaev.ideaservice.database.dto.IdeaShowDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IdeaService {
    void delete(Integer id, String userId, String url);
    void deleteFile(String filename, String userId);
    IdeaDto save(IdeaDto idea, String userId, MultipartFile file);
    List<IdeaDto> findAll();
    IdeaResponse findById(Integer id);
    IdeaShowDto findByIdWithFile(Integer id, String userId);
    void addRating(RatingDto rating, String token);
    List<RatingDto> findAllRatings(Integer ideaId, String token);

    void addComment(CommentDto commentDto, String tokenValue);

    List<CommentDto> findAllComments(Integer ideaId, String tokenValue);

    void updateRating(RatingRequest ratingRequest);
}
