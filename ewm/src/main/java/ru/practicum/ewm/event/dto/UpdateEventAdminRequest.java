package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.ewm.event.StateAction;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventAdminRequest {
    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;
    @Size(min = 20, max = 2000, message = "Annotation must be 20 to 2000 characters")
    private String annotation;
    @Size(min = 20, max = 7000, message = "Description must be 20 to 7000 characters")
    private String description;
    private Long category;
    private Boolean paid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private LocationDto location;
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;
}
