package com.daurenassanbaev.ideaservice.database.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdeaShowDto {
    private String title;
    private String description;
    private String url;
}