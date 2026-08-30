-- issue #846

-- newsletter_settings
INSERT INTO newsletter_settings (id, user_id, new_courses, course_updates, promotions,
                                 achievements, comments_replies, resume_views,
                                 vacancy_matches, community_news, marketing_tips,
                                 created_at, updated_at)
VALUES
    (1, 1, true, true, false, true, true, true, true, false, false,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 2, true, true, false, true, true, true, true, false, false,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 3, true, true, true, true, true, true, true, true, false,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Синхронизация identity-колонок после вставки фиксированных ID.
ALTER TABLE newsletter_settings ALTER COLUMN id RESTART WITH 4;
