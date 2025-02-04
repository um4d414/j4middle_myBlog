package ru.umd.myblog.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.umd.myblog.app.data.dto.PostDto;
import ru.umd.myblog.app.data.dto.PostsPage;
import ru.umd.myblog.app.data.entity.Post;
import ru.umd.myblog.app.data.repository.JdbcNativePostRepository;
import ru.umd.myblog.app.data.repository.PostRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultPostService implements PostService {
    private final PostRepository postRepository;

    @Override
    public List<PostDto> getPosts() {
        return postRepository
            .getAllPosts()
            .stream()
            .map(this::mapPost)
            .toList();
    }

    @Override
    public PostsPage getPosts(String tag, int page, int size) {
        int offset = page * size;
        List<Post> posts = postRepository.getPostsByTagWithPagination(tag, offset, size);
        int totalPosts = postRepository.countPostsByTag(tag);
        int totalPages = (int) Math.ceil((double) totalPosts / size);

        List<PostDto> dtos = posts.stream().map(this::mapPost).collect(Collectors.toList());

        return PostsPage.builder()
            .content(dtos)
            .currentPage(page)
            .pageSize(size)
            .totalPosts(totalPosts)
            .totalPages(totalPages)
            .build();
    }

    @Override
    public Optional<PostDto> findPostById(Long id) {
        return postRepository
            .getPost(id)
            .map(this::mapPost);
    }

    @Override
    public PostDto createPost(PostDto postDto) {
        Post post = mapPost(postDto);
        var resultPostDto = postRepository.createPost(post);
        return mapPost(resultPostDto);
    }

    @Override
    public int likePost(long id) {
        return postRepository.incrementLikes(id);
    }

    @Override
    public void updatePost(PostDto postDto) {
        postRepository.updatePost(mapPost(postDto));
    }

    @Override
    public void deletePost(long postId) {
        postRepository.deletePost(postId);
    }

    private PostDto mapPost(Post post) {
        return PostDto.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .imageUrl(post.getImageUrl())
            .likes(post.getLikes())
            .tags(post.getTags())
            .build();
    }

    private Post mapPost(PostDto postDto) {
        Post post = new Post();
        post.setId(postDto.getId());
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setImageUrl(postDto.getImageUrl());
        post.setLikes(postDto.getLikes());
        post.setTags(postDto.getTags());
        return post;
    }
}
