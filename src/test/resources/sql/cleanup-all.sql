SET REFERENTIAL_INTEGRITY FALSE;

DELETE FROM post_tags;
DELETE FROM comments;
DELETE FROM tags;
DELETE FROM posts;

ALTER TABLE posts ALTER COLUMN id RESTART WITH 1;
ALTER TABLE tags ALTER COLUMN id RESTART WITH 1;
ALTER TABLE comments ALTER COLUMN id RESTART WITH 1;

SET REFERENTIAL_INTEGRITY TRUE;