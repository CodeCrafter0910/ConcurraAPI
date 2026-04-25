package com.grid07.socialapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeRequest {
    private Long userId;
    private String userType;
}
