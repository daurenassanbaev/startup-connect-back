package com.daurenassanbaev.ideaservice.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingDto {
    private String userId;
    private Integer ideaId;
    private Integer score;
}
