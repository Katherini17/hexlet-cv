# Дизайн «Хекслет Карьера»

Новый дизайн продукта (job-search платформа). Источник истины — интерактивные прототипы в [`prototypes/`](prototypes/): откройте `app.dc.html` (приложение студента), `site.dc.html` (публичный сайт) или `admin.dc.html` (админка) в браузере — они самодостаточны и кликабельны.

- `screens/` — скриншоты всех экранов (сняты из прототипов), вставляются в issue.
- `annotated/` — 3 кадра с ручными пометками дизайнера.
- `prototypes/cookie-banner.html` — обычная HTML-страница, а не dc-канвас: маленький кадр, добавленный вне Claude Design. Открывается в браузере так же.
- Дорожная карта: майлстоуны M1–M7, эпики [#1119](https://github.com/hexlet-volunteers/hexlet-cv/issues/1119)–[#1132](https://github.com/hexlet-volunteers/hexlet-cv/issues/1132).

## Карта: экран → файл → issue

### Приложение студента (`app.dc.html`)

| Экран | Файл | Issue |
|---|---|---|
| Онбординг, шаг 1 | `screens/app-onboarding-step1.png` | #1142 |
| Онбординг, шаг 2 | `screens/app-onboarding-step2.png` | #1142 |
| Обзор (дашборд) | `screens/app-overview.png` | #1144, #1145 |
| Вакансии | `screens/app-vacancies.png` | #1151, #1152, #1153, #1154, #1149, #1150 |
| Источники | `screens/app-sources.png` | #1148, #1146 |
| Отклики — доска | `screens/app-tracker-kanban.png` | #1156, #1155 |
| Отклики — таблица | `screens/app-tracker-table.png` | #1157 |
| Отклики — drawer | `screens/app-tracker-drawer.png` | #1158 |
| Отклики — модалка добавления | `screens/app-tracker-add-modal.png` | #1159 |
| Зарплатный радар | `screens/app-salaries.png` | #1184, #1183 |
| Рефералы | `screens/app-referrals.png` | #1187, #1188, #1186 |
| Аналитика | `screens/app-analytics.png` | #1181, #1179 |
| Разбор отказов | `screens/app-rejections.png` | #1182, #1180 |
| Резюме | `screens/app-resume.png` | #1164, #1165, #1160, #1161, #1162 |
| Резюме — экспорт | `screens/app-resume-export-modal.png` | #1166, #1163 |
| Резюме — адаптация | `screens/app-resume-adapt-modal.png` | #1167 |
| Письма | `screens/app-letters.png` | #1169, #1168 |
| Профиль (публичный) | `screens/app-profile.png` | #1190, #1192, #802, #1189 |
| AI-собес | `screens/app-ai-interview.png` | #1172, #1170, #1171 |
| AI-собес — сессия | `screens/app-ai-interview-session.png` | #1172 |
| Живые моки | `screens/app-mocks.png` | #1174, #1175, #1173 |
| База вопросов | `screens/app-questions.png` | #1178, #1177 |
| Записи собесов | `screens/app-recordings.png` | #1176 |
| Комм. проекты | `screens/app-commercial-projects.png` | (студ. комм. проекты) |

### Публичный сайт (`site.dc.html`)

| Экран | Файл | Issue |
|---|---|---|
| Лендинг целиком | `screens/site-landing-full.png` | #1119 |
| Hero + промо-баннер + header | `screens/site-landing-hero.png` | #1133, #1138 |
| Возможности (2×2) | `screens/site-landing-features.png` | #1134 |
| Как это работает | `screens/site-landing-how.png` | #1135 |
| Тарифы | `screens/site-landing-pricing.png` | #1136 |
| CTA + footer | `screens/site-landing-cta-footer.png` | #792, #1136 |
| 404 | `screens/site-404.png` | #1139 |
| Правовая информация | `screens/site-legal.png` | #1140 |
| Блог — список | `screens/site-blog.png` | #1199, #1198 |
| Блог — статья | `screens/site-blog-article.png` | #1200 |
| Согласие на cookie | `prototypes/cookie-banner.html` | #1238 |

### Админка (`admin.dc.html`)

| Экран | Файл | Issue |
|---|---|---|
| Дашборд | `screens/admin-dashboard.png` | #1193 |
| Пользователи | `screens/admin-users.png` | #923, #1116 |
| Блог | `screens/admin-blog.png` | #1194 |
| Моки | `screens/admin-mocks.png` | #1196 |
| Заявки на комм. проекты | `screens/admin-projects.png` | #1195 |
| Источники и парсеры | `screens/admin-sources.png` | #1197 |

### Пометки дизайнера (`annotated/`)

| Файл | Что показывает |
|---|---|
| `annotated/resume-screen-annotated.png` | Экран «Резюме» с пометками |
| `annotated/ai-interview-training-annotated.png` | AI-собес / тренировки с пометками |
| `annotated/salary-radar-annotated.png` | Зарплатный радар с пометками |
