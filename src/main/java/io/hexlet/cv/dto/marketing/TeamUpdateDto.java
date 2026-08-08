package io.hexlet.cv.dto.marketing;

import io.hexlet.cv.model.enums.TeamMemberType;
import io.hexlet.cv.model.enums.TeamPosition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TeamUpdateDto {

    @Size(max = 255, message = "First name must not exceed 255 characters")
    private JsonNullable<String> firstName;

    @Size(max = 255, message = "Last name must not exceed 255 characters")
    private JsonNullable<String> lastName;

    private JsonNullable<TeamPosition> position;

    private JsonNullable<TeamMemberType> memberType;

    @URL(message = "Avatar URL must be valid")
    private JsonNullable<String> avatarUrl;

    private JsonNullable<Boolean> isPublished;

    private JsonNullable<Boolean> showOnHomepage;

    @Min(value = 0, message = "Display order must be positive")
    private JsonNullable<Integer> displayOrder;
}
