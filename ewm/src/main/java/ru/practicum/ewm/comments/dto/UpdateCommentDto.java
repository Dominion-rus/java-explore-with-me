package ru.practicum.ewm.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCommentDto {
    @NotBlank
    @Size(max = 1000)
    private String text;
}
