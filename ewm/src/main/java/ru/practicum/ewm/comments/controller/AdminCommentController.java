package ru.practicum.ewm.comments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping("/pending")
    public List<CommentDto> getPendingComments() {
        return commentService.getAllForModeration();
    }

    @PatchMapping("/{commentId}")
    public CommentDto moderateComment(@PathVariable Long commentId,
                                      @RequestParam boolean publish,
                                      @RequestParam(required = false) String moderatorComment) {
        return commentService.moderateComment(commentId, publish, moderatorComment);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAdmin(@PathVariable Long commentId) {
        commentService.deleteByAdmin(commentId);
    }
}
