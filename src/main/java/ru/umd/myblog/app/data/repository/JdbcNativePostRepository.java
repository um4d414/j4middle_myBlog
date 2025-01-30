package ru.umd.myblog.app.data.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.umd.myblog.app.data.entity.Post;
import ru.umd.myblog.app.data.entity.Tag;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
public class JdbcNativePostRepository implements PostRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Post> postRowMapper = (rs, rowNum) -> {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setTitle(rs.getString("title"));
        post.setImageUrl(rs.getString("image_url"));
        post.setContent(rs.getString("content"));
        post.setLikes(rs.getInt("likes"));
        return post;
    };

    private final RowMapper<Tag> tagRowMapper = (rs, rowNum) -> {
        Tag tag = new Tag();
        tag.setId(rs.getLong("id"));
        tag.setName(rs.getString("name"));
        return tag;
    };

    @Override
    public Optional<Post> getPost(long id) {
        var sql = """
                  SELECT id, title, image_url, content, likes FROM posts p
                  WHERE p.id = %d
                  """.formatted(id);

        var postQueryResult = jdbcTemplate.query(sql, postRowMapper);

        Post post;

        if (postQueryResult.isEmpty()) {
            return Optional.empty();
        } else {
            post = postQueryResult.getFirst();
        }

        var tagsSql = """
                      SELECT t.id, t.name FROM tags t
                      JOIN post_tags pt ON t.id = pt.tag_id
                      WHERE pt.post_id = %d""".formatted(post.getId());

        Set<Tag> tags = new HashSet<>(jdbcTemplate.query(tagsSql, tagRowMapper));
        post.setTags(tags);

        return Optional.of(post);
    }

    @Override
    public List<Post> getAllPosts() {
        var postSql = """
                      SELECT id, title, image_url, content, likes FROM posts p
                      """;

        List<Post> posts = jdbcTemplate.query(
            postSql, (rs, rowNum) -> {
                Post post = new Post();
                post.setId(rs.getLong("id"));
                post.setTitle(rs.getString("title"));
                post.setImageUrl(rs.getString("image_url"));
                post.setContent(rs.getString("content"));
                post.setLikes(rs.getInt("likes"));

                var tagsSql = """
                              SELECT t.id, t.name FROM tags t
                              JOIN post_tags pt ON t.id = pt.tag_id
                              WHERE pt.post_id = %d""".formatted(post.getId());

                Set<Tag> tags = new HashSet<>(jdbcTemplate.query(tagsSql, tagRowMapper));
                post.setTags(tags);
                return post;
            }
        );

        return posts;
    }

    @Override
    public Post createPost(Post post) {
        var sql = """
                  INSERT INTO posts (title, image_url, content)
                  VALUES (?, ?, ?)
                  """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
            connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, post.getTitle());
                ps.setString(2, post.getImageUrl());
                ps.setString(3, post.getContent());
                return ps;
            }, keyHolder
        );

        long postId = keyHolder.getKey().longValue();

        if (post.getTags() != null && !post.getTags().isEmpty()) {
            post.getTags().forEach(
                tag -> {
                    var tagName = tag.getName().trim();

                    String insertTagSql = "MERGE INTO tags (name) KEY (name) VALUES (?)\n";

                    jdbcTemplate.update(insertTagSql, tagName);

                    Long tagId = jdbcTemplate.queryForObject(
                        "SELECT id FROM tags WHERE name = ?", Long.class, tagName
                    );

                    String insertPostTagSql = """
                                              INSERT INTO post_tags (post_id, tag_id)
                                              VALUES (?, ?)
                                              """;
                    jdbcTemplate.update(insertPostTagSql, postId, tagId);
                }
            );
        }

        post.setId(postId);
        post.setComments(0);
        post.setLikes(0);

        return post;
    }

    @Override
    public int incrementLikes(Long postId) {
        String updateSql = "UPDATE posts SET likes = likes + 1 WHERE id = ?";
        jdbcTemplate.update(updateSql, postId);

        String selectSql = "SELECT likes FROM posts WHERE id = ?";
        return jdbcTemplate.queryForObject(selectSql, Integer.class, postId);
    }

    @Override
    public void updatePost(Post post) {
        String sql = "UPDATE posts SET title = ?, content = ?, image_url = ?, likes = ?, tags = ? WHERE id = ?";
        jdbcTemplate.update(
            sql,
            post.getTitle(),
            post.getContent(),
            post.getImageUrl(),
            post.getLikes(),
            post.getTags(),
            post.getId()
        );
    }
}
