package ru.umd.myblog.app.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.umd.myblog.app.config.web.ControllerTestConfig;
import ru.umd.myblog.app.data.dto.CommentDto;
import ru.umd.myblog.app.service.CommentService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {ControllerTestConfig.class})
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
public class CommentControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    // Получаем замокированный сервис для задания поведения в тестах
    @Autowired
    private CommentService commentService;

    @BeforeEach
    void setup() {
        // Создаем MockMvc с учетом настроек Spring MVC и view resolver (если используются шаблоны)
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // Тест для добавления комментария: POST /comments/{postId}
    @Test
    void shouldAddCommentAndRedirectToPostPage() throws Exception {
        long postId = 100L;
        String content = "Test comment";

        // В данном случае addComment возвращает void, поэтому не нужно настраивать возврат
        doNothing().when(commentService).addComment(eq(postId), eq(content));

        mockMvc.perform(post("/comments/{postId}", postId)
                            .param("content", content)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/posts/" + postId));

        verify(commentService, times(1)).addComment(eq(postId), eq(content));
    }

    // Тест для редактирования комментария: POST /comments/{commentId}/edit
    @Test
    void shouldEditCommentAndRedirectToPostPage() throws Exception {
        long commentId = 5L;
        long postId = 200L;
        String newContent = "Updated comment content";

        // Настраиваем сервис: при поиске комментария возвращается CommentDto с заданным postId
        CommentDto commentDto = CommentDto.builder()
            .id(commentId)
            .content("Old content")
            .postId(postId)
            .build();
        when(commentService.findCommentById(commentId)).thenReturn(Optional.of(commentDto));
        // updateComment вызывается без возврата, поэтому можно оставить doNothing()
        doNothing().when(commentService).updateComment(eq(commentId), eq(newContent));

        mockMvc.perform(post("/comments/{commentId}/edit", commentId)
                            .param("content", newContent)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/posts/" + postId));

        verify(commentService, times(1)).updateComment(eq(commentId), eq(newContent));
        verify(commentService, times(1)).findCommentById(eq(commentId));
    }

    // Тест для удаления комментария: POST /comments/{commentId}/delete
    @Test
    void shouldDeleteCommentAndRedirectToPostPage() throws Exception {
        long commentId = 7L;
        long postId = 300L;

        // При поиске комментария возвращаем CommentDto с заданным postId
        CommentDto commentDto = CommentDto.builder()
            .id(commentId)
            .content("Some comment")
            .postId(postId)
            .build();
        when(commentService.findCommentById(commentId)).thenReturn(Optional.of(commentDto));
        doNothing().when(commentService).deleteComment(commentId);

        mockMvc.perform(post("/comments/{commentId}/delete", commentId)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/posts/" + postId));

        verify(commentService, times(1)).deleteComment(eq(commentId));
        verify(commentService, times(1)).findCommentById(eq(commentId));
    }
}
