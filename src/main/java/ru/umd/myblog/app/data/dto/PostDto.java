package ru.umd.myblog.app.data.dto;

import lombok.*;
import lombok.experimental.Tolerate;

import java.util.Set;

@Data
@Builder
public class PostDto {
    private long id;

    private String title;

    private String imageUrl;

    private String content;

    private int likes;

    private Set<String> tags;

    private int comments;

    @Tolerate
    public PostDto() {}
}
