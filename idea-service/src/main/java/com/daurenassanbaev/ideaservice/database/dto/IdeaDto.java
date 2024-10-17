package com.daurenassanbaev.ideaservice.database.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdeaDto {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
