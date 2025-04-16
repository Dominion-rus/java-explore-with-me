package ru.practicum.ewm.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.EventSpecifications;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.dto.exceptions.ConflictException;
import ru.practicum.stats.dto.exceptions.NotFoundException;
import ru.practicum.stats.dto.exceptions.ValidateException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm.request.RequestStatus.CONFIRMED;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setViews(0L);

        return EventMapper.toFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("eventDate").descending());

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        return events.stream()
                .map(event -> {
                    long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), CONFIRMED);
                    long views = getViews(event);
                    return EventMapper.toShortDto(event, confirmedRequests, views);
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found or access denied"));

        Long confirmed = requestRepository.countByEventIdAndStatus(eventId, CONFIRMED);
        return EventMapper.toFullDto(event, confirmed);
    }


    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found or access denied"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot update published event");
        }

        if (updateRequest.getEventDate() != null && updateRequest
                .getEventDate()
                .isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidateException("Event date must be at least 2 hours from now");
        }

        EventMapper.updateEventFromDto(updateRequest, event);
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }

        return EventMapper.toFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> searchPublicEvents(PublicEventSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getFrom() / request.getSize(), request.getSize());

        List<Event> events = eventRepository.findAll(EventSpecifications.forPublicSearch(request), pageable).getContent();

        return events.stream()
                .map(event -> {
                    Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), CONFIRMED);
                    Long views = getViews(event); // если есть подсчёт через StatsClient
                    return EventMapper.toShortDto(event, views, confirmedRequests);
                })
                .collect(Collectors.toList());
    }


    @Override
    public EventFullDto getEventById(Long eventId, String ip, String uri) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event not published");
        }

        Long confirmed = requestRepository.countByEventIdAndStatus(eventId, CONFIRMED);
        Long views = getViews(event);

        return EventMapper.toFullDto(event, confirmed, views);
    }

    @Override
    public List<EventFullDto> searchEventsByAdmin(AdminEventSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getFrom() / request.getSize(), request.getSize());
        return eventRepository.findAll(EventSpecifications.forAdminSearch(request), pageable).stream()
                .map(event -> {
                    Long confirmed = requestRepository.countByEventIdAndStatus(event.getId(), CONFIRMED);
                    return EventMapper.toFullDto(event, confirmed);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }

        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidateException("Event date must be at least 1 hour from now");
        }

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Cannot publish the event because it's not in the right state: "
                                + event.getState());
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;

                case REJECT_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException("Cannot reject the event because it's not in the right state: "
                                + event.getState());
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        EventMapper.updateEventFromDto(request, event);

        Event saved = eventRepository.save(event);
        Long confirmed = requestRepository.countByEventIdAndStatus(eventId, CONFIRMED);

        return EventMapper.toFullDto(saved, confirmed);
    }

    private Long getViews(Event event) {
        String uri = "/events/" + event.getId();
        LocalDateTime start = event.getCreatedOn();
        LocalDateTime end = LocalDateTime.now();

        try {
            List<ViewStatsDto> stats = statsClient.getStats(start, end, List.of(uri), true);
            if (!stats.isEmpty()) {
                return stats.get(0).getHits();
            }
        } catch (Exception ex) {
            log.warn("Не удалось получить просмотры для события id={} по uri={}: {}", event.getId(), uri,
                    ex.getMessage());
        }

        return 0L;
    }

}