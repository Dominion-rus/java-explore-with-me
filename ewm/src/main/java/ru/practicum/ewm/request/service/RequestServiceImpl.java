package ru.practicum.ewm.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.RequestStatus;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.RequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import ru.practicum.stats.dto.exceptions.ConflictException;
import ru.practicum.stats.dto.exceptions.NotFoundException;
import ru.practicum.stats.dto.exceptions.ValidateException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Event is not published");
        }

        if (requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Request already exists");
        }

        if (event.getParticipantLimit() > 0 &&
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED) >= event
                        .getParticipantLimit()) {
            throw new ConflictException("Participant limit reached");
        }
        RequestStatus status;
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = RequestStatus.PENDING;
        }

        Request request = Request.builder()
                .event(event)
                .requester(user)
                .status(status)
                .created(LocalDateTime.now())
                .build();

        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<RequestDto> getUserRequests(Long userId) {
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ValidateException("Cannot cancel request from another user");
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            throw new ConflictException("Cannot cancel confirmed request");
        }

        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<RequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidateException("Only initiator can view event requests");
        }

        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId,
                                                                EventRequestStatusUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidateException("Only event initiator can update request statuses");
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ValidateException("Request moderation not required or no participant limit");
        }

        List<Request> requests = requestRepository.findAllById(request.getRequestIds());

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        long limit = event.getParticipantLimit();

        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            if (confirmedCount + requests.size() > limit) {
                throw new ConflictException("Participant limit has been reached");
            }
        }

        if (!request.getStatus().equals(RequestStatus.CONFIRMED) &&
                !request.getStatus().equals(RequestStatus.REJECTED)) {
            throw new ConflictException("Unsupported status update");
        }

        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        for (Request r : requests) {
            if (!r.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Only pending requests can be updated");
            }

            if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                if (confirmedCount >= limit) {
                    r.setStatus(RequestStatus.REJECTED);
                    rejected.add(r);
                } else {
                    r.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(r);
                    confirmedCount++;
                }
            } else if (request.getStatus().equals(RequestStatus.REJECTED)) {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(r);
            }
        }

        requestRepository.saveAll(requests);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed.stream().map(RequestMapper::toDto).toList())
                .rejectedRequests(rejected.stream().map(RequestMapper::toDto).toList())
                .build();
    }

}
