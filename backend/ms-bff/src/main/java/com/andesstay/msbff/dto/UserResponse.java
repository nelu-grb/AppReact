package com.andesstay.msbff.dto;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String preferredUsername;
}
