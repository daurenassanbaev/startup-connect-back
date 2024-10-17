package com.daurenassanbaev.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserShowDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String url;
}
