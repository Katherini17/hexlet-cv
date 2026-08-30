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
public class ArticleCreateDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 50000, message = "Content must not exceed 50000 characters")
    private String content;

    @URL(message = "Image URL must be valid")
    private String imageUrl;

    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    @Min(value = 1, message = "Reading time must be at least 1 minute")
    private Integer readingTime;

    @NotNull(message = "Published status is required")
    private Boolean isPublished = false;

    @Size(max = 255, message = "Home component ID must not exceed 255 characters")
    private String homeComponentId;

    @NotNull(message = "Homepage visibility is required")
    private Boolean showOnHomepage = false;

    @Min(value = 0, message = "Display order must be positive")
    private Integer displayOrder = 0;
}
