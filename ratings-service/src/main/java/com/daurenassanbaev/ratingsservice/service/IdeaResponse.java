package com.daurenassanbaev.ratingsservice.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdeaResponse {
    private String userId;
    private String title;
}
