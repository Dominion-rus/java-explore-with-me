package ru.practicum.ewm.comments.service;

import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.PublicCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto);

    void deleteComment(Long userId, Long commentId);

    List<PublicCommentDto> getCommentsByEvent(Long eventId);

    List<CommentDto> getUserComments(Long userId);

    // Админские методы
    CommentDto moderateComment(Long commentId, boolean publish, String moderatorComment);

    List<CommentDto> getAllForModeration();

    void deleteByAdmin(Long commentId);
}
