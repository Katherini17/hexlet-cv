package io.hexlet.cv.dto.marketing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class ReviewCreateDto {
    @NotBlank(message = "Author name is required")
    @Size(max = 255, message = "Author name must not exceed 255 characters")
    private String author;

    @NotBlank(message = "Review content is required")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    @URL(message = "Avatar URL must be valid")
    private String avatarUrl;

    private Boolean isPublished = false;

    @NotNull(message = "Homepage visibility is required")
    private Boolean showOnHomepage;

    @Min(value = 0, message = "Display order must be positive")
    private Integer displayOrder = 0;
}
