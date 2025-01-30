package ru.umd.myblog.app.data.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.umd.myblog.app.data.entity.Post;

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
                      SELECT t.name FROM tags t
                      JOIN post_tags pt ON t.id = pt.tag_id
                      WHERE pt.post_id = %d""".formatted(post.getId());

        Set<String> tags = new HashSet<>(jdbcTemplate.queryForList(tagsSql, String.class));
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
                              SELECT t.name FROM tags t
                              JOIN post_tags pt ON t.id = pt.tag_id
                              WHERE pt.post_id = %d""".formatted(post.getId());

                Set<String> tags = new HashSet<>(jdbcTemplate.queryForList(tagsSql, String.class));
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
                tagName -> {
                    tagName = tagName.trim();

                    String insertTagSql = "MERGE INTO tags (name) KEY (name) VALUES (?)";
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
        post.setLikes(0);

        return post;
    }

    @Override
    public int incrementLikes(long postId) {
        String updateSql = "UPDATE posts SET likes = likes + 1 WHERE id = ?";
        jdbcTemplate.update(updateSql, postId);

        String selectSql = "SELECT likes FROM posts WHERE id = ?";
        return jdbcTemplate.queryForObject(selectSql, Integer.class, postId);
    }

    @Override
    @Transactional
    public void updatePost(Post post) {
        var getLikesSql = "SELECT likes FROM posts WHERE id = ?";
        var currentLikes = jdbcTemplate.queryForObject(getLikesSql, Integer.class, post.getId());

        int likesToUpdate = (post.getLikes() != 0) ? post.getLikes() : currentLikes;

        String updatePostSql = "UPDATE posts SET title = ?, content = ?, image_url = ?, likes = ? WHERE id = ?";
        jdbcTemplate.update(
            updatePostSql,
            post.getTitle(),
            post.getContent(),
            post.getImageUrl(),
            likesToUpdate,
            post.getId()
        );

        var deletePostTagsSql = "DELETE FROM post_tags WHERE post_id = ?";
        jdbcTemplate.update(deletePostTagsSql, post.getId());

        if (post.getTags() != null && !post.getTags().isEmpty()) {
            for (String tagName : post.getTags()) {
                tagName = tagName.trim();

                String insertTagSql = "MERGE INTO tags (name) KEY (name) VALUES (?)";
                jdbcTemplate.update(insertTagSql, tagName);

                Long tagId = jdbcTemplate.queryForObject(
                    "SELECT id FROM tags WHERE name = ?", Long.class, tagName
                );

                String insertPostTagSql = "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)";
                jdbcTemplate.update(insertPostTagSql, post.getId(), tagId);
            }
        }
    }

    @Transactional
    @Override
    public void deletePost(long postId) {
        String checkPostSql = "SELECT COUNT(*) FROM posts WHERE id = ?";
        int count = jdbcTemplate.queryForObject(checkPostSql, Integer.class, postId);
        if (count == 0) {
            throw new RuntimeException("Пост с ID " + postId + " не найден");
        }

        String deletePostTagsSql = "DELETE FROM post_tags WHERE post_id = ?";
        jdbcTemplate.update(deletePostTagsSql, postId);

        String deleteCommentsSql = "DELETE FROM comments WHERE post_id = ?";
        jdbcTemplate.update(deleteCommentsSql, postId);

        String deletePostSql = "DELETE FROM posts WHERE id = ?";
        jdbcTemplate.update(deletePostSql, postId);
    }
}
