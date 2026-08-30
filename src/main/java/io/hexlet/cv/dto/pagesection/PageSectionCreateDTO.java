package io.hexlet.cv.dto.pagesection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PageSectionCreateDTO {

    // "main", "profile" и др.
    @NotBlank(message = "Page key is required")
    @Size(max = 255, message = "Page key must not exceed 255 characters")
    private String pageKey;

    // "about_us", "team", "pricing" и др.
    @NotBlank(message = "Section key is required")
    @Size(max = 255, message = "Section key must not exceed 255 characters")
    private String sectionKey;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 50000, message = "Content must not exceed 50000 characters")
    private String content;

    // Можно не указывать, включена ли секция - сервис установит true по умолчанию
    private Boolean active;
}
