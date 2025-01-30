package ru.umd.myblog.app.controller;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.umd.myblog.app.data.dto.PostDto;
import ru.umd.myblog.app.service.PostService;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping(
        value = "/posts",
        produces = MediaType.TEXT_HTML_VALUE
    )
    public String postsFeed(Model model) {
        model.addAttribute("posts", postService.getPosts());
        model.addAttribute("post", new PostDto());

        return "postfeed";
    }

    @PostMapping(
        value = "/posts",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.TEXT_HTML_VALUE
    )
    public String createPost(
        @ModelAttribute("post") PostDto postDto,
        @RequestParam("image") MultipartFile image
    ) throws IOException {
        if (!image.isEmpty()) {
            String imageUrl = saveImage(image);
            postDto.setImageUrl(imageUrl);
        }

        postService.createPost(postDto);

        return "redirect:/posts";
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable("postId") Long postId) throws NotFoundException {
        int newLikesCount;
        try {
            newLikesCount = postService.likePost(postId);
        } catch (Exception e) {
            throw new NotFoundException("Пост не найден");
        }

        return ResponseEntity.ok(new LikesResponse(newLikesCount));
    }

    @GetMapping("/posts/{postId}")
    public String getPost(@PathVariable("postId") Long postId, Model model) throws NotFoundException {
        var post = postService
            .findPost(postId)
            .orElseThrow(() -> new NotFoundException("Пост не найден"));

        model.addAttribute("post", post);

        return "post";
    }
//
//    @PostMapping("/posts/{postId}/edit")
//    public String editPost(@PathVariable("postId") Long postId, @ModelAttribute("post") PostDto postDto) {
//        postService.updatePost(postId, postDto);
//
//        return "redirect:/posts/" + postId; // Перенаправляем на страницу поста
//    }
//
//    @PostMapping("/posts/{postId}/delete")
//    public String deletePost(@PathVariable("postId") Long postId) {
//        postService.deletePost(postId); // Удаляем пост
//        return "redirect:/posts"; // Перенаправляем на главную страницу
//    }

    private String saveImage(MultipartFile image) throws IOException {
        var uploadDir = "file:/C:/uploads/";
        var resource = new UrlResource(uploadDir);

        if (!resource.exists()) {
            resource.getFile().mkdirs();
        }

        var fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        var file = new File(resource.getFile(), fileName);

        image.transferTo(file);

        return uploadDir + fileName;
    }

    record LikesResponse(int likes) {
    }
}
