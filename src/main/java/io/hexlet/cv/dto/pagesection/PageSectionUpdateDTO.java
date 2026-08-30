package io.hexlet.cv.dto.pagesection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageSectionUpdateDTO {

    @NotBlank(message = "Техническое название страницы с секцией обязательно")
    @Size(max = 255, message = "Page key must not exceed 255 characters")
    private JsonNullable<String> pageKey;

    @NotBlank(message = "Техническое название обязательно")
    @Size(max = 255, message = "Section key must not exceed 255 characters")
    private JsonNullable<String> sectionKey;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private JsonNullable<String> title;
    @Size(max = 50000, message = "Content must not exceed 50000 characters")
    private JsonNullable<String> content;

    @NotNull(message = "Статус активности секции обязателен")
    private JsonNullable<Boolean> active;
}
