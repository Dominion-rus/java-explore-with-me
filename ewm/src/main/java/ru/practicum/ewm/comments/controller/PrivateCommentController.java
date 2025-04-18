package ru.practicum.ewm.comments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.comments.dto.UpdateCommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/{eventId}")
    public ResponseEntity<CommentDto> createComment(@PathVariable Long userId,
                                                    @PathVariable Long eventId,
                                                    @RequestBody @Valid NewCommentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(userId, eventId, dto));
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @RequestBody @Valid UpdateCommentDto dto) {
        return commentService.updateComment(userId, commentId, dto);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long userId,
                                              @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<CommentDto> getUserComments(@PathVariable Long userId) {
        return commentService.getUserComments(userId);
    }
}

