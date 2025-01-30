package ru.umd.myblog.app.data.dto;

import lombok.*;
import lombok.experimental.Tolerate;

@Data
@Builder
public class CommentDto {
    private long id;

    private String content;

    private long postId;

    @Tolerate
    public CommentDto() {}
}
