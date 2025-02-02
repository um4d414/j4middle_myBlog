package ru.umd.myblog.app.controller;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.umd.myblog.app.data.dto.CommentDto;
import ru.umd.myblog.app.data.dto.PostDto;
import ru.umd.myblog.app.service.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    private final CommentService commentService;

    private final ImageService imageService;

    @GetMapping(
        produces = MediaType.TEXT_HTML_VALUE
    )
    public String postsFeed(Model model) {
        model.addAttribute("posts", postService.getPosts());
        model.addAttribute("post", new PostDto());

        return "postfeed";
    }

    @PostMapping(
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.TEXT_HTML_VALUE
    )
    public String createPost(
        @ModelAttribute("post") PostDto postDto,
        @RequestParam("image") MultipartFile image
    ) throws IOException {
        if (!image.isEmpty()) {
            String imageUrl = imageService.saveImage(image);
            postDto.setImageUrl(imageUrl);
        }

        postService.createPost(postDto);

        return "redirect:/posts";
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable("postId") Long postId) throws NotFoundException {
        int newLikesCount;
        try {
            newLikesCount = postService.likePost(postId);
        } catch (Exception e) {
            throw new NotFoundException("Пост не найден");
        }

        return ResponseEntity.ok(new LikesResponse(newLikesCount));
    }

    @GetMapping("/{postId}")
    public String getPost(@PathVariable("postId") Long postId, Model model) throws NotFoundException {
        var post = postService
            .findPostById(postId)
            .orElseThrow(() -> new NotFoundException("Пост не найден"));

        List<CommentDto> comments = commentService.getCommentsByPostId(postId);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);

        return "post";
    }

    @PostMapping("/edit")
    public String editPost(@ModelAttribute("post") PostDto postDto) {
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
