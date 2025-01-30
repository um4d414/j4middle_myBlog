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
public class PostService {
    private final PostRepository postRepository;

    public List<PostDto> getPosts() {
        return postRepository
            .getAllPosts()
            .stream()
            .map(this::mapPost)
            .toList();
    }

    public Optional<PostDto> findPost(Long id) {
        return postRepository
            .getPost(id)
            .map(this::mapPost);
    }

    public PostDto createPost(PostDto postDto) {
        Post post = mapPost(postDto);
        var resultPostDto = postRepository.createPost(post);
        return mapPost(resultPostDto);
    }

    public int likePost(Long id) {
        return postRepository.incrementLikes(id);
    }

    public void updatePost(PostDto postDto) {
        postRepository.updatePost(mapPost(postDto));
    }

    private PostDto mapPost(Post post) {
        return PostDto
            .builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .imageUrl(post.getImageUrl())
            .likes(post.getLikes())
            .comments(post.getComments())
            .build();
    }

    private Post mapPost(PostDto postDto) {
        var post = new Post();
        post.setId(postDto.getId());
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setImageUrl(postDto.getImageUrl());
        post.setLikes(postDto.getLikes());
        post.setComments(postDto.getComments());

        return post;
    }
}
