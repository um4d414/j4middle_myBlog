package ru.umd.myblog.app.data.repository;

import ru.umd.myblog.app.data.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Optional<Post> getPost(long id);

    List<Post> getAllPosts();

    List<Post> getPostsByTagWithPagination(String tag, int offset, int limit);

    int countPostsByTag(String tag);

    Post createPost(Post post);

    int incrementLikes(long postId);

    void updatePost(Post post);

    void deletePost(long postId);
}
