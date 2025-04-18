package ru.practicum.ewm.comments.mapper;

import ru.practicum.ewm.comments.CommentStatus;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.PublicCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.user.mapper.UserMapper;

import java.time.LocalDateTime;

public class CommentMapper {

    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreatedOn())
                .updated(comment.getEditedOn())
                .published(comment.getStatus() == CommentStatus.APPROVED)
                .event(EventMapper.toShortDto(comment.getEvent()))
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .build();
    }

    public static Comment toEntity(NewCommentDto dto) {
        return Comment.builder()
                .text(dto.getText())
                .createdOn(LocalDateTime.now())
                .status(CommentStatus.PENDING)
                .build();
    }

    public static void update(Comment comment, UpdateCommentDto dto) {
        comment.setText(dto.getText());
        comment.setEditedOn(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);
    }

    public static CommentDto toDto(Comment comment, Long currentUserId) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreatedOn())
                .updated(comment.getEditedOn())
                .published(comment.getStatus() == CommentStatus.APPROVED)
                .event(EventMapper.toShortDto(comment.getEvent()))
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .moderatorComment(
                        comment.getAuthor().getId().equals(currentUserId) ? comment.getModeratorComment() : null
                )
                .build();
    }

    public static PublicCommentDto toPublicDto(Comment comment) {
        return PublicCommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreatedOn())
                .updated(comment.getEditedOn())
                .published(comment.getStatus() == CommentStatus.APPROVED)
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .build();
    }
}
