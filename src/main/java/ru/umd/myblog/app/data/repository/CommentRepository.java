package ru.umd.myblog.app.data.repository;

import ru.umd.myblog.app.data.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    List<Comment> findByPostId(long postId);

    Comment save(Comment comment);

    void deleteById(long commentId);

    void update(Comment comment);

    Optional<Comment> findById(long commentId);
}
