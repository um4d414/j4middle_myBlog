package ru.umd.myblog.app.service;

import ru.umd.myblog.app.data.dto.PostDto;

import java.util.List;
import java.util.Optional;

public interface PostService {
    List<PostDto> getPosts();

    Optional<PostDto> findPostById(Long id);

    PostDto createPost(PostDto postDto);

    int likePost(long id);

    void updatePost(PostDto postDto);

    void deletePost(long postId);
}
