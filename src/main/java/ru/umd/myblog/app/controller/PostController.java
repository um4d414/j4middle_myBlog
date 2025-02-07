package ru.umd.myblog.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.umd.myblog.app.data.dto.CommentDto;
import ru.umd.myblog.app.data.dto.PostDto;
import ru.umd.myblog.app.service.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    private final CommentService commentService;

    private final ImageService imageService;

    @GetMapping
    public String postsFeed(
        @RequestParam(value = "tag", required = false) String tag,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        Model model
    ) {
        var postsPage = postService.getPosts(tag, page, size);
        model.addAttribute("posts", postsPage.getContent());
        model.addAttribute("currentPage", postsPage.getCurrentPage());
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("pageSize", postsPage.getPageSize());
        model.addAttribute("tag", tag);

        model.addAttribute("post", PostDto.builder().build());

        return "postfeed";
    }

    @PostMapping
    public String createPost(
        @ModelAttribute("post") PostDto postDto,
        @RequestParam(value = "tagsString", required = false) String tagsString,
        @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        if (tagsString != null && !tagsString.isBlank()) {
            Set<String> tags = Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
            postDto.setTags(tags);
        }
        if (!image.isEmpty()) {
            String imageUrl = imageService.saveImage(image);
            postDto.setImageUrl(imageUrl);
        }
        postService.createPost(postDto);
        return "redirect:/posts";
    }

    @PostMapping("/{postId}/like")
    public String likePost(
        @PathVariable("postId") Long postId,
        @RequestHeader(value = "Referer", required = false) String referer
    ) {
        postService.likePost(postId);

        if (referer == null || referer.isBlank()) {
            return "redirect:/posts";
        }
        return "redirect:" + referer;
    }

    @GetMapping("/{postId}")
    public String getPost(@PathVariable("postId") Long postId, Model model) {
        var post = postService
            .findPostById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пост не найден"));

        List<CommentDto> comments = commentService.getCommentsByPostId(postId);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);

        return "post";
    }

    @PostMapping("/edit")
    public String editPost(
        @ModelAttribute("post") PostDto postDto,
        @RequestParam(value = "tags", required = false) String tagsString,
        @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        if (!image.isEmpty()) {
            String imageUrl = imageService.saveImage(image);
            postDto.setImageUrl(imageUrl);
        }

        if (tagsString != null && !tagsString.isBlank()) {
            Set<String> tags = Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
            postDto.setTags(tags);
        }

        postService.updatePost(postDto);

        return "redirect:/posts/" + postDto.getId();
    }

    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable("postId") Long postId) {
        postService.deletePost(postId);
        return "redirect:/posts";
    }

    record LikesResponse(int likes) {
    }
}
