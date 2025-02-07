package ru.umd.myblog.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.umd.myblog.app.service.CommentService;

@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/{postId}")
    public String addComment(@PathVariable("postId") long postId, @RequestParam("content") String content) {
        commentService.addComment(postId, content);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{commentId}/edit")
    public String editComment(
        @PathVariable("commentId") long commentId,
        @RequestParam("content") String content
    ) {
        commentService.updateComment(commentId, content);

        var comment = commentService
            .findCommentById(commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пост не найден"));

        return "redirect:/posts/" + comment.getPostId(); // Перенаправляем на страницу поста
    }

    @PostMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable("commentId") long commentId) {
        var comment = commentService
            .findCommentById(commentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Комментарий не найден"));

        commentService.deleteComment(commentId);
        return "redirect:/posts/" + comment.getPostId();
    }
}