package com.daurenassanbaev.ratingsservice.mapper;

import com.daurenassanbaev.ratingsservice.database.dto.RatingDto;
import com.daurenassanbaev.ratingsservice.database.entity.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingDtoMapper implements Mapper<Rating, RatingDto> {

    public void copy(Rating dto, RatingDto rating) {
        rating.setIdeaId(dto.getIdeaId());
        rating.setUserId(dto.getUserId());
        rating.setScore(dto.getScore());
        rating.setId(dto.getId());
    }

    @Override
    public RatingDto map(Rating object) {
        RatingDto rating = new RatingDto();
        copy(object, rating);
        return rating;
    }
}
