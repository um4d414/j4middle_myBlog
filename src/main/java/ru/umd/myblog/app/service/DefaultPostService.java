package ru.umd.myblog.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.umd.myblog.app.data.dto.PostDto;
import ru.umd.myblog.app.data.entity.Post;
import ru.umd.myblog.app.data.repository.PostRepository;

import java.util.List;
import java.util.Optional;

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
        return PostDto
            .builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .imageUrl(post.getImageUrl())
            .likes(post.getLikes())
            .tags(post.getTags()) // Теги уже в виде Set<String>
            .build();
    }

    private Post mapPost(PostDto postDto) {
        var post = new Post();
        post.setId(postDto.getId());
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setImageUrl(postDto.getImageUrl());
        post.setLikes(postDto.getLikes());
        post.setTags(postDto.getTags()); // Теги уже в виде Set<String>
        return post;
    }
}
