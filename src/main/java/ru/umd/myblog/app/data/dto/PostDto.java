package ru.umd.myblog.app.data.dto;

import lombok.*;
import lombok.experimental.Tolerate;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class PostDto {
    private long id;

    private String title;

    private String imageUrl;

    private String content;

    private int likes;

    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Tolerate
    public PostDto() {}
}
