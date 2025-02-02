package ru.umd.myblog.app.service;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.umd.myblog.app.data.dto.CommentDto;
import ru.umd.myblog.app.data.entity.Comment;
import ru.umd.myblog.app.data.repository.CommentRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Primary
public class DefaultCommentService implements CommentService {
    private final CommentRepository commentRepository;

    @Override
    public List<CommentDto> getCommentsByPostId(long postId) {
        return commentRepository.findByPostId(postId).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    public void addComment(long postId, String content) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPostId(postId);

        commentRepository.save(comment);
    }

    @Override
    public void updateComment(long commentId, String content) throws NotFoundException {
        Comment comment = commentRepository
            .findById(commentId)
            .orElseThrow(() -> new NotFoundException("Комментарий с ID " + commentId + " не найден"));

        comment.setContent(content);
        commentRepository.update(comment);
    }

    @Override
    public void deleteComment(long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    public Optional<CommentDto> findCommentById(long commentId) {
        return commentRepository
            .findById(commentId)
            .map(this::mapToDto);
    }

    private CommentDto mapToDto(Comment comment) {
        return CommentDto.builder()
            .id(comment.getId())
            .content(comment.getContent())
            .postId(comment.getPostId())
            .build();
    }
}
