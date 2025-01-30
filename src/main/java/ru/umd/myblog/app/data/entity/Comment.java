package ru.umd.myblog.app.data.entity;

import lombok.*;

@Data
public class Comment {
    private long id;

    private String content;

    private Long postId;
}
