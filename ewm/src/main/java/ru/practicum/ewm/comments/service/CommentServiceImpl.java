package ru.practicum.ewm.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comments.CommentStatus;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.PublicCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;
import ru.practicum.ewm.comments.mapper.CommentMapper;
import ru.practicum.ewm.comments.model.Comment;
import ru.practicum.ewm.comments.repository.CommentRepository;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.stats.dto.exceptions.ConflictException;
import ru.practicum.stats.dto.exceptions.NotFoundException;
import ru.practicum.stats.dto.exceptions.ValidateException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidateException("You can only comment on published events");
        }

        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        boolean recent = commentRepository.existsByAuthorIdAndEventIdAndCreatedOnAfter(userId, eventId, oneMinuteAgo);
        if (recent) {
            throw new ValidateException("You can leave only one comment per minute on this event.");
        }

        Comment comment = Comment.builder()
                .author(user)
                .event(event)
                .text(dto.getText())
                .createdOn(LocalDateTime.now())
                .status(CommentStatus.PENDING)
                .build();

        return CommentMapper.toDto(commentRepository.save(comment), userId);
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {
        Comment comment = getOwnedCommentOrThrow(userId, commentId);

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ValidateException("You can only edit comments that are pending moderation");
        }

        comment.setText(dto.getText());
        comment.setEditedOn(LocalDateTime.now());
        comment.setStatus(CommentStatus.PENDING);
        comment.setModeratorComment(null);
        comment.setEditedOn(LocalDateTime.now());

        return CommentMapper.toDto(commentRepository.save(comment), userId);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getOwnedCommentOrThrow(userId, commentId);
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicCommentDto> getCommentsByEvent(Long eventId) {
        return commentRepository.findByEventIdAndStatus(eventId, CommentStatus.APPROVED).stream()
                .map(CommentMapper::toPublicDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long userId) {
        return commentRepository.findByAuthorId(userId).stream()
                .map(comment -> CommentMapper.toDto(comment, userId))
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto moderateComment(Long commentId, boolean publish, String moderatorComment) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (publish && comment.getStatus() == CommentStatus.APPROVED) {
            throw new ConflictException("Comment is already published");
        }

        comment.setStatus(publish ? CommentStatus.APPROVED : CommentStatus.REJECTED);
        comment.setModeratorComment(moderatorComment);
        comment.setEditedOn(LocalDateTime.now());
        //return CommentMapper.toDto(commentRepository.save(comment));
        return CommentMapper.toDto(comment, comment.getAuthor().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllForModeration() {
        return commentRepository.findByStatus(CommentStatus.PENDING).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    private Comment getOwnedCommentOrThrow(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ValidateException("User is not the author of this comment");
        }
        return comment;
    }

    @Override
    public void deleteByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        commentRepository.delete(comment);
    }
}

