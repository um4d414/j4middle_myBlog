INSERT INTO posts (id, title, image_url, content, likes)
VALUES (1, 'First Post', 'http://example.com/image1.jpg', 'Content of the first post', 10);

INSERT INTO comments (content, post_id)
VALUES
    ('Сomment 1 content', 1),
    ('Сomment 2 content', 1);

ALTER TABLE comments ALTER COLUMN id RESTART WITH 3;