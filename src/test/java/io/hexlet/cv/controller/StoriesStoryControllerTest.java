package io.hexlet.cv.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexlet.cv.dto.StoriesStoryDto;
import io.hexlet.cv.dto.StoriesStoryPageResponse;
import io.hexlet.cv.model.StoriesStory;
import io.hexlet.cv.repository.StoriesStoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the public stories endpoint.
 *
 * Improvements:
 * - Extracted base URL to a constant to avoid duplication and make intent clear.
 * - Added @DisplayName to make test intent explicit in reports.
 * - Set Accept header to application/json to be explicit about expected content type.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class StoriesStoryControllerTest {

    private static final String BASE_URL = "/api/v1/stories";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoriesStoryRepository storiesStoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void cleanUp() {
        storiesStoryRepository.deleteAll();
    }

    private StoriesStory createRandomStory(boolean isPublished, int displayOrder) {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        return storiesStoryRepository.save(StoriesStory.builder()
                .authorName("Студент_" + uniqueId)
                .avatarUrl("https://example.com_" + uniqueId + ".jpg")
                .companyName("Компания_" + uniqueId)
                .offerPosition("Разработчик_" + uniqueId)
                .text("Тестовый текст отзыва номер " + uniqueId)
                .displayOrder(displayOrder)
                .isPublished(isPublished)
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/stories — пустая выборка")
    public void testGetPublicStoriesEmpty() throws Exception {
        String body = mockMvc.perform(get(BASE_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        StoriesStoryPageResponse actualPage = objectMapper.readValue(body, StoriesStoryPageResponse.class);

        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getContent()).isEmpty();
        assertThat(actualPage.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("GET /api/v1/stories — сортировка и пагинация по умолчанию")
    public void testGetPublicStoriesDefaultSortingAndPagination() throws Exception {
        var firstStory = createRandomStory(true, 1);
        var secondStory = createRandomStory(true, 2);
        createRandomStory(false, 3); // draft — must not be included

        String body = mockMvc.perform(get(BASE_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        StoriesStoryPageResponse actualPage = objectMapper.readValue(body, StoriesStoryPageResponse.class);

        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getContent()).hasSize(2);

        // Ensure ordering by displayOrder ascending (firstStory.displayOrder == 1)
        assertThat(actualPage.getContent())
                .extracting(StoriesStoryDto::getAuthorName)
                .containsExactly(firstStory.getAuthorName(), secondStory.getAuthorName());

        assertThat(actualPage.getTotalElements()).isEqualTo(2);
        assertThat(actualPage.getTotalPages()).isEqualTo(1);
        assertThat(actualPage.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("GET /api/v1/stories — кастомная сортировка")
    public void testGetPublicStoriesCustomSortingAndSize() throws Exception {
        createRandomStory(true, 1);
        var expectedFirstOnDescPage = createRandomStory(true, 2);

        String body = mockMvc.perform(get(BASE_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "displayOrder,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        StoriesStoryPageResponse actualPage = objectMapper.readValue(body, StoriesStoryPageResponse.class);

        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getContent()).hasSize(1);

        assertThat(actualPage.getContent())
                .extracting(StoriesStoryDto::getDisplayOrder)
                .containsExactly(expectedFirstOnDescPage.getDisplayOrder());

        assertThat(actualPage.getTotalElements()).isEqualTo(2);
        assertThat(actualPage.getNumber()).isZero();
    }
}