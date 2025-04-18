package ru.practicum.ewm.comments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comments.dto.PublicCommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/{eventId}/comments")
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<PublicCommentDto> getCommentsByEvent(@PathVariable Long eventId) {
        return commentService.getCommentsByEvent(eventId);
    }
}
