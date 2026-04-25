package com.grid07.socialapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    private Long authorId;
    private String authorType;
    private String content;
    private int depthLevel;
    private Long postOwnerId;
}
