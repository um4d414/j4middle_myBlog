package ru.umd.myblog.app.data.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.umd.myblog.app.data.dto.CommentDto;
import ru.umd.myblog.app.data.entity.Comment;

@Repository
@Primary
@RequiredArgsConstructor
public class JdbcNativeCommentRepository implements CommentRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Comment> commentRowMapper = (rs, rowNum) -> {
        Comment comment = new Comment();
        comment.setId(rs.getLong("id"));
        comment.setContent(rs.getString("content"));
        comment.setPostId(rs.getLong("post_id"));
        return comment;
    };


    @Override
    public void addComment(Long postId, String content) {

    }

    @Override
    public void updateComment(Long commentId, String content) {

    }

    @Override
    public void deleteComment(Long commentId) {

    }

    @Override
    public CommentDto getCommentById(Long commentId) {
        return null;
    }

    @Override
    public Long getPostIdByCommentId(Long commentId) {
        return 0L;
    }
}
