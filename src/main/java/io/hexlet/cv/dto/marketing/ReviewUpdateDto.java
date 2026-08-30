package io.hexlet.cv.dto.marketing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class ReviewUpdateDto {

    @Size(max = 255, message = "Author name must not exceed 255 characters")
    private JsonNullable<String> author;

    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private JsonNullable<String> content;

    @URL(message = "Avatar URL must be valid")
    private JsonNullable<String> avatarUrl;

    private JsonNullable<Boolean> isPublished;

    private JsonNullable<Boolean> showOnHomepage;

    @Min(value = 0, message = "Display order must be positive")
    private JsonNullable<Integer> displayOrder;
}
