package io.hexlet.cv.dto.marketing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class StoryUpdateDto {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private JsonNullable<String> title;

    @Size(max = 50000, message = "Content must not exceed 50000 characters")
    private JsonNullable<String> content;

    @URL(message = "Image URL must be valid")
    private JsonNullable<String> imageUrl;

    private JsonNullable<Boolean> isPublished;

    private JsonNullable<Boolean> showOnHomepage;

    @Min(value = 0, message = "Display order must be positive")
    private JsonNullable<Integer> displayOrder;
}
