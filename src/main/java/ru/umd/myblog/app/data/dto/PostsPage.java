package ru.umd.myblog.app.data.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostsPage {
    private List<PostDto> content;

    private int currentPage;

    private int pageSize;

    private int totalPosts;

    private int totalPages;
}