package com.daurenassanbaev.commentservice.db.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Integer id;
    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotNull(message = "Idea ID cannot be null")
    private Integer ideaId;

    @NotBlank(message = "Content cannot be blank")
    @Size(max = 1000, message = "Content cannot exceed 1000 characters")
    private String content;
}
