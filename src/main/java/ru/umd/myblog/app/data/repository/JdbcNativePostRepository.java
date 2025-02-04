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
import java.sql.Statement;
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
    public List<Post> getPostsByTagWithPagination(String tag, int offset, int limit) {
        String sql;
        Object[] params;
        if (tag != null && !tag.isBlank()) {
            sql = "SELECT DISTINCT p.id, p.title, p.image_url, p.content, p.likes " +
                  "FROM posts p " +
                  "JOIN post_tags pt ON p.id = pt.post_id " +
                  "JOIN tags t ON pt.tag_id = t.id " +
                  "WHERE t.name = ?" +
                  "ORDER BY p.id DESC " +
                  "LIMIT ? OFFSET ?";
            params = new Object[]{tag, limit, offset};
        } else {
            sql = "SELECT id, title, image_url, content, likes FROM posts ORDER BY id DESC LIMIT ? OFFSET ?";
            params = new Object[]{limit, offset};
        }
        List<Post> posts = jdbcTemplate.query(sql, params, postRowMapper);
        // Для каждого поста получаем теги (если их нужно, можно оставить как есть)
        for (Post post : posts) {
            String tagsSql = "SELECT t.name FROM tags t " +
                             "JOIN post_tags pt ON t.id = pt.tag_id " +
                             "WHERE pt.post_id = ?";
            Set<String> tags = new HashSet<>(jdbcTemplate.queryForList(tagsSql, String.class, post.getId()));
            post.setTags(tags);
        }
        return posts;
    }

    @Override
    public int countPostsByTag(String tag) {
        String sql;
        Object[] params;
        if (tag != null && !tag.isBlank()) {
            sql = "SELECT COUNT(DISTINCT p.id) " +
                  "FROM posts p " +
                  "JOIN post_tags pt ON p.id = pt.post_id " +
                  "JOIN tags t ON pt.tag_id = t.id " +
                  "WHERE t.name LIKE ?";
            params = new Object[]{"%" + tag + "%"};
        } else {
            sql = "SELECT COUNT(*) FROM posts";
            params = new Object[]{};
        }
        return jdbcTemplate.queryForObject(sql, params, Integer.class);
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
        // Получаем текущее количество лайков, если новое значение не задано
        String getLikesSql = "SELECT likes FROM posts WHERE id = ?";
        Integer currentLikes = jdbcTemplate.queryForObject(getLikesSql, Integer.class, post.getId());
        int likesToUpdate = (post.getLikes() != 0) ? post.getLikes() : currentLikes;

        // Обновляем данные поста
        String updatePostSql = "UPDATE posts SET title = ?, content = ?, image_url = ?, likes = ? WHERE id = ?";
        jdbcTemplate.update(
            updatePostSql,
            post.getTitle(),
            post.getContent(),
            post.getImageUrl(),
            likesToUpdate,
            post.getId()
        );

        // Удаляем старые связи с тегами
        String deletePostTagsSql = "DELETE FROM post_tags WHERE post_id = ?";
        jdbcTemplate.update(deletePostTagsSql, post.getId());

        // Если заданы новые теги, добавляем их и создаем связи
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            for (String rawTag : post.getTags()) {
                final String tagName = rawTag.trim();

                // Проверяем, существует ли тег
                String findTagSql = "SELECT id FROM tags WHERE name = ?";
                List<Long> tagIds = jdbcTemplate.queryForList(findTagSql, Long.class, tagName);

                Long tagId;
                if (tagIds.isEmpty()) {
                    // Если тег не существует, вставляем его с использованием KeyHolder для получения сгенерированного ID
                    String insertTagSql = "INSERT INTO tags (name) VALUES (?)";
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    jdbcTemplate.update(
                        connection -> {
                            PreparedStatement ps = connection.prepareStatement(
                                insertTagSql,
                                Statement.RETURN_GENERATED_KEYS
                            );
                            ps.setString(1, tagName);
                            return ps;
                        }, keyHolder
                    );
                    tagId = Objects.requireNonNull(keyHolder.getKey()).longValue();
                } else {
                    // Если тег уже есть, используем его ID
                    tagId = tagIds.get(0);
                }

                // Создаем связь между постом и тегом
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
