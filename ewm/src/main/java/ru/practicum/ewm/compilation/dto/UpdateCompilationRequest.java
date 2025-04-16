package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompilationRequest {
    @Size(max = 50, message = "Title must be no longer than 50 characters")
    private String title;
    private Boolean pinned;
    private List<Long> events;
}

