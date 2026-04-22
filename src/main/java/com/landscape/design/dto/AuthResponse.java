package com.landscape.design.dto;

public record AuthResponse(
        String token,
        UserInfoDto user
) {}
