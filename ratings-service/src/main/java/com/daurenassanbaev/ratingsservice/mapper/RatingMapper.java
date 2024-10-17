package com.daurenassanbaev.ratingsservice.mapper;

import com.daurenassanbaev.ratingsservice.database.dto.RatingDto;
import com.daurenassanbaev.ratingsservice.database.entity.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper implements Mapper<RatingDto, Rating> {
    public void copy(RatingDto dto, Rating rating) {
        rating.setIdeaId(dto.getIdeaId());
        rating.setUserId(dto.getUserId());
        rating.setScore(dto.getScore());
    }

    @Override
    public Rating map(RatingDto object) {
        Rating rating = new Rating();
        copy(object, rating);
        return rating;
    }
}
