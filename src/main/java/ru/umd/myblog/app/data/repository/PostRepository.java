package ru.umd.myblog.app.data.repository;

import ru.umd.myblog.app.data.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Optional<Post> getPost(long id);

    List<Post> getAllPosts();

    Post createPost(Post post);

    int incrementLikes(Long postId);

    void updatePost(Post post);
}
