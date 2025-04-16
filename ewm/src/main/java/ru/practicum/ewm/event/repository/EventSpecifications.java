package ru.practicum.ewm.event.repository;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.EventState;
import ru.practicum.ewm.event.dto.AdminEventSearchRequest;
import ru.practicum.ewm.event.dto.PublicEventSearchRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class EventSpecifications {

    public Specification<Event> hasUsers(List<User> users) {
        return (root, query, builder) ->
                users == null || users.isEmpty() ? null : root.get("initiator").in(users);
    }

    public Specification<Event> hasStates(List<EventState> states) {
        return (root, query, builder) ->
                states == null || states.isEmpty() ? null : root.get("state").in(states);
    }

    public Specification<Event> hasCategories(List<Category> categories) {
        return (root, query, builder) ->
                categories == null || categories.isEmpty() ? null : root.get("category").in(categories);
    }

    public Specification<Event> after(LocalDateTime start) {
        return (root, query, builder) ->
                start == null ? null : builder.greaterThanOrEqualTo(root.get("eventDate"), start);
    }

    public Specification<Event> before(LocalDateTime end) {
        return (root, query, builder) ->
                end == null ? null : builder.lessThanOrEqualTo(root.get("eventDate"), end);
    }

    public Specification<Event> containsText(String text) {
        return (root, query, builder) -> {
            if (text == null || text.isBlank()) return null;
            String likePattern = "%" + text.toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("annotation")), likePattern),
                    builder.like(builder.lower(root.get("description")), likePattern)
            );
        };
    }

    public Specification<Event> isPaid(Boolean paid) {
        return (root, query, builder) ->
                paid == null ? null : builder.equal(root.get("paid"), paid);
    }

    public Specification<Event> isPublished() {
        return (root, query, builder) ->
                builder.equal(root.get("state"), EventState.PUBLISHED);
    }

    public Specification<Event> forAdminSearch(AdminEventSearchRequest request) {
        List<User> users = request.getUsers() == null ? null : request.getUsers().stream()
                .map(id -> {
                    User u = new User();
                    u.setId(id);
                    return u;
                }).collect(Collectors.toList());

        List<Category> categories = request.getCategories() == null ? null : request.getCategories().stream()
                .map(id -> {
                    Category c = new Category();
                    c.setId(id);
                    return c;
                }).collect(Collectors.toList());

        return Specification.where(hasUsers(users))
                .and(hasStates(request.getStates()))
                .and(hasCategories(categories))
                .and(after(request.getRangeStart()))
                .and(before(request.getRangeEnd()));
    }

    public Specification<Event> forPublicSearch(PublicEventSearchRequest request) {
        return Specification.where(containsText(request.getText()))
                .and(hasCategories(request.getCategories() == null ? null : request.getCategories().stream()
                        .map(id -> {
                            Category c = new Category();
                            c.setId(id);
                            return c;
                        }).collect(Collectors.toList())))
                .and(isPaid(request.getPaid()))
                .and(after(request.getRangeStart()))
                .and(before(request.getRangeEnd()))
                .and(isPublished());
    }
}