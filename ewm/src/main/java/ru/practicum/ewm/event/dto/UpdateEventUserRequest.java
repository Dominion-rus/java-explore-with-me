package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.ewm.event.StateAction;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventUserRequest {
    @Size(min = 3, max = 120, message = "Title must be 3 to 120 characters")
    private String title;
    @Size(min = 20, max = 2000, message = "Annotation must be between 20 and 2000 characters")
    private String annotation;
    @Size(min = 20, max = 7000, message = "Description must be 20 to 7000 characters")
    private String description;
    private Long category;
    private Boolean paid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private LocationDto location;
    @Min(value = 0, message = "participantLimit must be zero or positive")
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;
}

