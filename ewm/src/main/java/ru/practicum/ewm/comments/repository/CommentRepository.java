package ru.practicum.ewm.comments.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.comments.CommentStatus;
import ru.practicum.ewm.comments.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status);

    List<Comment> findByAuthorId(Long authorId);

    List<Comment> findByStatus(CommentStatus status);

    boolean existsByAuthorIdAndText(Long authorId, String text);

    List<Comment> findAllByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);

    boolean existsByAuthorIdAndEventIdAndCreatedOnAfter(Long userId, Long eventId, LocalDateTime time);

}
