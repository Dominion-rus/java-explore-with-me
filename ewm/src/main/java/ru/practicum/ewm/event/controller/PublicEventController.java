package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.PublicEventSearchRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.exceptions.BadRequestException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    public List<EventShortDto> getPublicEvents(@Valid PublicEventSearchRequest request,
                                               HttpServletRequest servletRequest) {

        if (request.getRangeEnd() != null && request.getRangeStart() != null &&
                request.getRangeEnd().isBefore(request.getRangeStart())) {
            throw new BadRequestException("rangeEnd must not be before rangeStart");
        }

        sendStatsHit(servletRequest);

        return eventService.searchPublicEvents(request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable Long id,
                                     HttpServletRequest servletRequest) {

        sendStatsHit(servletRequest);

        return eventService.getEventById(id, servletRequest.getRemoteAddr(), servletRequest.getRequestURI());
    }

    private void sendStatsHit(HttpServletRequest request) {
        statsClient.saveHit(EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }
}
