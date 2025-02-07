package ru.umd.myblog.app.data.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.umd.myblog.app.data.dto.CommentDto;
import ru.umd.myblog.app.data.entity.Comment;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
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
    public List<Comment> findByPostId(long postId) {
        String sql = "SELECT id, content, post_id FROM comments WHERE post_id = ?";
        return jdbcTemplate.query(sql, commentRowMapper, postId);
    }

    @Override
    public Comment save(Comment comment) {
        String sql = "INSERT INTO comments (content, post_id) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, comment.getContent());
            ps.setLong(2, comment.getPostId());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        comment.setId(id);

        return comment;
    }

    @Override
    public void deleteById(long commentId) {
        String sql = "DELETE FROM comments WHERE id = ?";
        jdbcTemplate.update(sql, commentId);
    }

    @Override
    public void update(Comment comment) {
        String sql = "UPDATE comments SET content = ? WHERE id = ?";
        jdbcTemplate.update(sql, comment.getContent(), comment.getId());
    }

    @Override
    public Optional<Comment> findById(long commentId) {
        String sql = "SELECT id, content, post_id FROM comments WHERE id = ?";
        return jdbcTemplate.query(sql, commentRowMapper, commentId).stream().findFirst();
    }
}
