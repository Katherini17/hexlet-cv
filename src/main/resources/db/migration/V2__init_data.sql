-- users
-- Пароль seed-пользователей: qweqweqwe
-- Хранится в виде BCrypt-хэша, сгенерированного BCryptPasswordEncoder
INSERT INTO users (id, email, first_name, last_name, encrypted_password,
                   sign_in_count, current_sign_in_at, current_sign_in_ip,
                   role, state, locale, created_at, updated_at)
VALUES
    (1, 'ivan@google.com', 'Иван', 'Иванов',
     '$2a$10$5ZcF/IRoH4X1gicCg.Be9OWt7eDHkY.SSy42D0XWjpwumbAeCv00S',
     1, CURRENT_TIMESTAMP, '127.0.0.1', 'CANDIDATE', 'active', 'ru',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'olga@yandex.ru', 'Ольга', 'Петрова',
     '$2a$10$5ZcF/IRoH4X1gicCg.Be9OWt7eDHkY.SSy42D0XWjpwumbAeCv00S',
     1, CURRENT_TIMESTAMP, '127.0.0.1', 'CANDIDATE', 'active', 'ru',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'sergey@gmail.com', 'Сергей', 'Сидоров',
     '$2a$10$5ZcF/IRoH4X1gicCg.Be9OWt7eDHkY.SSy42D0XWjpwumbAeCv00S',
     1, CURRENT_TIMESTAMP, '127.0.0.1', 'ADMIN', 'active', 'ru',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


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


-- resumes
INSERT INTO resumes (id, user_id, name, state, answers_count, locale, city,
                     relocation, contact, contact_phone, contact_email,
                     contact_telegram, evaluated_ai, summary,
                     created_at, updated_at)
VALUES
    (1, 1, 'Резюме backend-разработчика', 'draft', 0, 'ru', 'Москва',
     'возможен', 'telegram', '+7-900-123-45-67', 'ivan@google.com', '@иван',
     false, 'Это резюме Иван Иванов тут тело самого резюме лалала',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 2, 'Резюме frontend-разработчика', 'draft', 0, 'ru', 'Москва',
     'возможен', 'telegram', '+7-900-123-45-67', 'olga@yandex.ru', '@ольга',
     false, 'Это резюме Ольга Петрова тут тело самого резюме лалала',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- resume_works
INSERT INTO resume_works (id, resume_id, company, position, begin_date, end_date,
                          current, description, company_description,
                          created_at, updated_at)
VALUES
    (1, 1, 'Hexlet', 'Java Developer',
     CURRENT_DATE - 730, CURRENT_DATE, false,
     'Работа в проекте', 'Крупная компания', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 2, 'Яндекс', 'Frontend Developer',
     CURRENT_DATE - 730, CURRENT_DATE, false,
     'Работа в проекте', 'Крупная компания', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- resume_educations
INSERT INTO resume_educations (id, resume_id, institution, faculty, begin_date,
                               end_date, current, description,
                               created_at, updated_at)
VALUES
    (1, 1, 'МГУ', 'Факультет ВМК',
     CURRENT_DATE - 2190, CURRENT_DATE - 730, false,
     'Обучение в университете', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 2, 'СПбГУ', 'Прикладная математика',
     CURRENT_DATE - 2190, CURRENT_DATE - 730, false,
     'Обучение в университете', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- resume_answers (olgaResume, автор — ivan)
INSERT INTO resume_answers (id, resume_id, user_id, content, likes_count,
                            applying_state, created_at, updated_at)
VALUES
    (1, 2, 1, 'Отличное резюме, но стоит уточнить опыт в Spring Boot.', 0,
     'reviewed', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- resume_answer_likes (лайк от sergey)
INSERT INTO resume_answer_likes (id, answer_id, resume_id, user_id,
                                 created_at, updated_at)
VALUES
    (1, 1, 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- resume_answer_comments (комментарий sergey к ответу, answer_user — ivan)
INSERT INTO resume_answer_comments (id, answer_id, resume_id, user_id,
                                    answer_user_id, content,
                                    created_at, updated_at)
VALUES
    (1, 1, 2, 3, 1, 'Полностью согласен, добавить детали по проектам.',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- resume_comments (комментарий sergey к резюме ivan)
INSERT INTO resume_comments (id, resume_id, user_id, content,
                             created_at, updated_at)
VALUES
    (1, 1, 3, 'Хорошее резюме, но желательно добавить больше деталей об опыте.',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- careers
INSERT INTO careers (id, name, description, slug, locale, created_at, updated_at)
VALUES
    (1, 'Java разработчик', 'Путь развития разработчика', 'java-dev', 'ru',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- career_steps
INSERT INTO career_steps (id, name, description, tasks_text, review_needed,
                          locale, notification_kind, created_at, updated_at)
VALUES
    (1, 'Основы Java', 'Изучение базового синтаксиса',
     'Пройти курс, выполнить задания', true, 'ru', 'email',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- career_items
INSERT INTO career_items (id, career_id, career_step_id, "order", created_at, updated_at)
VALUES
    (1, 1, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- career_members
INSERT INTO career_members (id, career_id, user_id, state, finished_at, created_at, updated_at)
VALUES
    (1, 1, 1, 'in_progress', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- career_step_members
INSERT INTO career_step_members (id, career_step_id, career_member_id, state,
                                 created_at, updated_at)
VALUES
    (1, 1, 1, 'started', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- countries / vacancies
INSERT INTO countries (id, name, created_at, updated_at)
VALUES
    (1, 'Россия', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO vacancies (id, creator_id, title, kind, state, company_name,
                       programming_language, location, city_name, country_id,
                       salary_from, salary_to, employment_type, position_level,
                       salary_currency, salary_amount_type, locale, published_at,
                       created_at, updated_at)
VALUES
    (1, 3, 'Java Developer в Hexlet', 'fulltime', 'published', 'Hexlet',
     'Java', 'Москва', 'Москва', 1, 150000, 250000, 'office', 'middle',
     'RUB', 'gross', 'ru', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- events
INSERT INTO events (id, user_id, kind, locale, state, resource_type, resource_id,
                    created_at, updated_at)
VALUES
    (1, 1, 'resume_created', 'ru', 'done', 'Resume', 1,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- notifications
INSERT INTO notifications (id, user_id, resource_type, resource_id, state, kind,
                           created_at, updated_at)
VALUES
    (1, 1, 'Resume', 1, 'new', 'new_comment', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- impressions
-- impressionable_id = 1 был захардкожен в DataInitializer
INSERT INTO impressions (id, user_id, controller_name, action_name, view_name,
                         impressionable_type, impressionable_id, ip_address,
                         session_hash, request_hash, message,
                         created_at, updated_at)
VALUES
    (1, 1, 'ResumesController', 'show', 'resume_view', 'Resume', 1, '127.0.0.1',
     'sess-123', 'req-123', 'Просмотр резюме', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- tags
INSERT INTO tags (id, name, taggings_count, created_at, updated_at)
VALUES
    (1, 'Java', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- taggings
-- tagger_id = 1 был захардкожен в DataInitializer
INSERT INTO taggings (id, tag_id, taggable_type, taggable_id, tagger_type,
                      tagger_id, context, tenant, created_at)
VALUES
    (1, 1, 'Resume', 1, 'User', 1, 'default', 'default', CURRENT_TIMESTAMP);


-- versions
INSERT INTO versions (id, item_type, item_id, event, whodunnit, object, created_at)
VALUES
    (1, 'Resume', 1, 'create', 'system', 'object-data', CURRENT_TIMESTAMP);


-- career_member_versions
INSERT INTO career_member_versions (id, career_member_id, item_type, item_id,
                                    event, whodunnit, created_at)
VALUES
    (1, 1, 'CareerMember', 1, 'create', 'system', CURRENT_TIMESTAMP);


-- Синхронизация identity-колонок после вставки фиксированных ID.
ALTER TABLE users ALTER COLUMN id RESTART WITH 4;
ALTER TABLE newsletter_settings ALTER COLUMN id RESTART WITH 4;

ALTER TABLE resumes ALTER COLUMN id RESTART WITH 3;
ALTER TABLE resume_works ALTER COLUMN id RESTART WITH 3;
ALTER TABLE resume_educations ALTER COLUMN id RESTART WITH 3;

ALTER TABLE resume_answers ALTER COLUMN id RESTART WITH 2;
ALTER TABLE resume_answer_likes ALTER COLUMN id RESTART WITH 2;
ALTER TABLE resume_answer_comments ALTER COLUMN id RESTART WITH 2;
ALTER TABLE resume_comments ALTER COLUMN id RESTART WITH 2;

ALTER TABLE careers ALTER COLUMN id RESTART WITH 2;
ALTER TABLE career_steps ALTER COLUMN id RESTART WITH 2;
ALTER TABLE career_items ALTER COLUMN id RESTART WITH 2;
ALTER TABLE career_members ALTER COLUMN id RESTART WITH 2;
ALTER TABLE career_step_members ALTER COLUMN id RESTART WITH 2;

ALTER TABLE countries ALTER COLUMN id RESTART WITH 2;
ALTER TABLE vacancies ALTER COLUMN id RESTART WITH 2;
ALTER TABLE events ALTER COLUMN id RESTART WITH 2;
ALTER TABLE notifications ALTER COLUMN id RESTART WITH 2;
ALTER TABLE impressions ALTER COLUMN id RESTART WITH 2;
ALTER TABLE tags ALTER COLUMN id RESTART WITH 2;
ALTER TABLE taggings ALTER COLUMN id RESTART WITH 2;
ALTER TABLE versions ALTER COLUMN id RESTART WITH 2;
ALTER TABLE career_member_versions ALTER COLUMN id RESTART WITH 2;