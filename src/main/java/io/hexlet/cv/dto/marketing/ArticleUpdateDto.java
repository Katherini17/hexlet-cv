package io.hexlet.cv.dto.marketing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class ArticleUpdateDto {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private JsonNullable<String> title;

    @Size(max = 50000, message = "Content must not exceed 50000 characters")
    private JsonNullable<String> content;

    @URL(message = "Image URL must be valid")
    private JsonNullable<String> imageUrl;

    @Size(max = 255, message = "Author must not exceed 255 characters")
    private JsonNullable<String> author;

    @Min(value = 1, message = "Reading time must be at least 1 minute")
    private JsonNullable<Integer> readingTime;

    private JsonNullable<Boolean> isPublished;

    @Size(max = 255, message = "Home component ID must not exceed 255 characters")
    private JsonNullable<String> homeComponentId;

    private JsonNullable<Boolean> showOnHomepage;

    @Min(value = 0, message = "Display order must be positive")
    private JsonNullable<Integer> displayOrder;
}
