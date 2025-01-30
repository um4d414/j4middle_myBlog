package ru.umd.myblog.app.controller;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
    ) throws NotFoundException {
        commentService.updateComment(commentId, content);

        var comment = commentService
            .findCommentById(commentId)
            .orElseThrow();

        return "redirect:/posts/" + comment.getPostId(); // Перенаправляем на страницу поста
    }

    @PostMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable("commentId") long commentId) throws NotFoundException {
        var comment = commentService
            .findCommentById(commentId)
            .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        commentService.deleteComment(commentId);
        return "redirect:/posts/" + comment.getPostId();
    }
}