package ru.umd.myblog.app.data.dto;

import lombok.*;

@Data
public class CommentDto {
    private long id;

    private String content;

    private PostDto post;
}
