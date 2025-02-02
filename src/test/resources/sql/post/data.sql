-- Вставляем посты
INSERT INTO posts (title, image_url, content, likes)
VALUES ('First Post', 'http://example.com/image1.jpg', 'Content of the first post', 10),
       ('Second Post', 'http://example.com/image2.jpg', 'Content of the second post', 5);

ALTER TABLE posts ALTER COLUMN id RESTART WITH 3;

-- Вставляем теги без явного указания id
INSERT INTO tags (name)
VALUES ('tech'),
       ('java'),
       ('spring'),
       ('jdbc');

ALTER TABLE tags ALTER COLUMN id RESTART WITH 5;

-- Вставляем связи между постами и тегами
INSERT INTO post_tags (post_id, tag_id)
VALUES (1, 1),
       (1, 2),
       (2, 3),
       (2, 4);