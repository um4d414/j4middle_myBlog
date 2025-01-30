package ru.umd.myblog.app.data.repository;

import ru.umd.myblog.app.data.entity.Comment;

import java.util.List;

public interface CommentRepository {
    void addComment(Long postId, String content);

    void updateComment(Long commentId, String content);

    void deleteComment(Long commentId); //

    List<Comment> findByPostId(Long postId);
}
