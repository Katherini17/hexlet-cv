package io.hexlet.cv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stories_stories")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoriesStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String authorName;

    private String avatarUrl;

    private String companyName;

    private String offerPosition;

    @Column(name = "text", columnDefinition = "TEXT", nullable = false)
    private String text;

    private Integer displayOrder;

    @Column(name = "is_published", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isPublished = false;

}