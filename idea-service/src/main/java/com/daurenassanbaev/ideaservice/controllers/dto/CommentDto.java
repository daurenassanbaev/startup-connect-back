package com.daurenassanbaev.ideaservice.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private String userId;
    private Integer ideaId;
    private String content;
}
