# #1215 — Ротация и отзыв refresh-токенов: план реализации

**Дата:** 25.08.2026
**Задача:** [#1215](https://github.com/hexlet-volunteers/hexlet-cv/issues/1215) — харденинг JWT и cookie (часть эпика #1213, майлстоун M1)
**Повод:** замечание @arthur810629 к смерженному PR #1260 — ротация не гасит предъявленный refresh-токен
**Автор документа:** Claude Code (дизайн), реализация — Textile86
**Статус:** реализовано

Документ самодостаточный: диагноз, код, план релиза, тесты, чеклист приёмки. Другие варианты решения здесь не разбираются — только тот, который предлагается делать, и обоснование каждого решения.

---

## 1. Где мы сейчас по всем пунктам #1215

Статус каждого подпункта задачи #1215 (на момент реализации):

| Подпункт #1215 | Статус | Где |
|---|---|---|
| Cookie: `HttpOnly`, `Secure`, `SameSite` | ✅ сделано | [CookieProperties.java](../src/main/java/io/hexlet/cv/config/CookieProperties.java), [application.yml:41-52](../src/main/resources/application.yml#L41-L52) |
| Cookie: ограниченный `Path` | ❌ **не сделано** | `path: /` у обеих cookie — [application-prod.yml:19](../src/main/resources/application-prod.yml#L19), [:25](../src/main/resources/application-prod.yml#L25). См. 9.4 |
| Короткий TTL access-токена | ✅ сделано | 900 с — [application.yml:53](../src/main/resources/application.yml#L53) |
| **Ротация refresh-токена** | ❌ **не сделано** | предмет этого документа |
| Отзыв при logout | ⚠️ частично | работает, но глобально: выкидывает со всех устройств |
| Отзыв при смене пароля | ⛔ **невозможно** | флоу смены пароля не существует: `CustomUserDetailsService.changePassword` — заглушка `UnsupportedOperationException` ([CustomUserDetailsService.java:39-41](../src/main/java/io/hexlet/cv/service/CustomUserDetailsService.java#L39-L41)). См. 10.1 |
| Подпись ключом из секрета, не хардкод | ✅ сделано | RSA из файла, путь через env; `certs/` и `*.pem` в `.gitignore` (проверил `git ls-files` — ключей в репозитории нет) |
| Алгоритм фиксирован | ✅ сделано | `NimbusJwtDecoder.withPublicKey(...)` — RS256, `alg` из заголовка не выбирается |
| Проверка `exp`/`iss`/`aud` | ✅ сделано | [EncodersConfig.java:94-102](../src/main/java/io/hexlet/cv/config/EncodersConfig.java#L94-L102) + `JwtValidators.createDefault()` |

**Вывод:** этот PR закрывает «ротацию» и переводит «отзыв при logout» из глобального в per-session. Два подпункта (`Path` и смена пароля) остаются открытыми — см. раздел 10.

---

## 2. Замечание ревьюера верно. Проверка по коду

Ревьюер прав по всем трём следствиям. Подтверждаю построчно.

**Ротация без отзыва** — [TokenService.java:30-42](../src/main/java/io/hexlet/cv/security/TokenService.java#L30-L42): декодирование, затем сразу выдача новой пары. Предъявленный токен нигде не помечается использованным.

**Единственный механизм отзыва — глобальный** — `incrementTokenVersion` ([UserRepository.java:15-17](../src/main/java/io/hexlet/cv/repository/UserRepository.java#L15-L17)) инкрементит одно поле `User.tokenVersion`, вызывается только из `revokeByRefreshToken` (логаут).

**`jti` в токене нет** — [JWTUtils.java:43-56](../src/main/java/io/hexlet/cv/util/JWTUtils.java#L43-L56) кладёт `type`, `tokenVersion`, `iss/aud/sub/exp/iat`. Уникального идентификатора нет, значит сервер не может отличить первое предъявление токена от повторного.

Следствия ровно те, что описал ревьюер: украденная cookie работает все 30 дней, повторное использование не детектится, окно жизни украденного токена не сужается. Плюс `tokenVersion` — один на пользователя, поэтому «погасить только эту сессию» текущей схемой не выражается.

Из двух вариантов, предложенных ревьюером (`jti` + таблица либо `tokenVersion` на устройство), выбираю первый: **только он даёт детект повторного использования.** `tokenVersion` на устройство требует такой же персистентной таблицы, но не отвечает на вопрос «этот конкретный токен уже обменивали?» — то есть кражу по-прежнему не видно. Раз таблица нужна в обоих случаях, берём вариант, который даёт больше.

---

## 3. Решение: модель и обоснование

### 3.1 Что добавляется в токен

- `jti` (стандартный claim, `JwtClaimsSet.id`) — уникальный идентификатор конкретного экземпляра refresh-токена;
- `familyId` — идентификатор цепочки ротаций одной сессии. Один логин = одно семейство = одно устройство. Ротация меняет `jti`, сохраняя `familyId`.

`familyId` кладётся **и в access-токен** — обоснование в 3.4.

### 3.2 Что добавляется в БД

Таблица `refresh_tokens`: одна строка на каждый выданный refresh-токен. Строка — это ответ на вопрос «этот токен ещё не обменивали?».

Инвариант: **в живом семействе активна ровно одна строка** — последняя выданная. Все предшественники погашены.

### 3.3 Почему `revokedAt`, а не enum `status`

Состояние строки хранится как `Instant revokedAt` (`null` = активна), а не как `enum Status { ACTIVE, REVOKED }`. Три причины:

1. **Нет проблемы enum-литералов в JPQL.** `update ... set t.status = 'REVOKED'` — это поведение, за которое приходится ручаться версией Hibernate (у нас 6.6 через Boot 3.5.0, [libs.versions.toml:18](../gradle/libs.versions.toml#L18)). `where t.revokedAt is null` работает при любой версии и в любой СУБД, а у нас их две — H2 в dev/test и PostgreSQL в prod.
2. **Время отзыва получается бесплатно**, а оно нужно и для аудита (#1223), и для отсечки гонок (3.5).
3. Enum из двух значений с гарантией «обратно не переходим» — это и есть nullable timestamp, только дороже в описании.

Плюс поле `replacedByJti` — цепочка ротаций становится проходимой в обе стороны. Это даёт следы аудита («какой токен чем заменён») и нужно для 3.5.

### 3.4 Почему `familyId` в access-токене

Без этого **после logout access-токен живёт до 15 минут** — таблица `refresh_tokens` про access-токены ничего не знает.

Сейчас такой дыры нет: logout инкрементит `tokenVersion`, а валидатор `tokenVersionValid()` навешен и на access-декодер, и на refresh-декодер ([EncodersConfig.java:50-61](../src/main/java/io/hexlet/cv/config/EncodersConfig.java#L50-L61)). То есть logout мгновенно убивает оба токена. Если при переходе на per-session отзыв просто заменить инкремент `tokenVersion` на отзыв семейства, мы **потеряем** синхронную инвалидацию access-токена. Это был бы регресс безопасности, привнесённый улучшением безопасности.

Возражение «JWT stateless, не будем ходить в БД на каждый запрос» здесь неприменимо: **`tokenVersionValid()` уже делает `findByEmail` на каждый запрос** — за поход в БД давно заплачено. Причём заплачено дороже, чем нужно: `findByEmail` поднимает всю сущность `User` ради сравнения одного числа.

Поэтому: заменяю на access-декодере `tokenVersionValid()` единым валидатором `accessSessionValid()`, который **одним** запросом проверяет и `tokenVersion`, и живость семейства (4.7). Итог по стоимости — один запрос на запрос, как и было, только вместо загрузки сущности — `count > 0`. Итог по безопасности:

- logout гасит одну сессию, access-токен мёртв немедленно;
- детект кражи гасит семейство вместе с его access-токенами — реакция мгновенная, а не «через 15 минут»;
- `tokenVersion` остаётся чистым глобальным kill switch.

Важная деталь про стоимость: `cookieTokenResolver` ([SecurityConfig.java:134-147](../src/main/java/io/hexlet/cv/config/SecurityConfig.java#L134-L147)) отдаёт `access_token` из cookie на **любом** запросе, где эта cookie есть — включая `permitAll`-маршруты. Значит валидатор выполняется на всём трафике аутентифицированного пользователя, не только на `/account/**`. Именно поэтому важно, что запрос остался один, а не стало два.

### 3.5 Гонки честного клиента

Отзыв всего семейства при повторном предъявлении — правильное поведение по OAuth 2.0 Security BCP. Но у него есть цена, которую надо назвать прямо: **два параллельных refresh одним токеном — это норма, а не атака.** Две открытые вкладки, ретрай после обрыва сети, двойной клик — и человек разлогинен со всего устройства. Refresh-cookie имеет `SameSite=Lax`, так что сценарий с несколькими вкладками абсолютно реален.

Два уровня защиты:

1. **Обязательно, на фронте:** single-flight — один refresh в полёте, остальные ждут его результат. Без этого детект будет стрелять по своим. Требование выносится в раздел 6 как блокирующее.
2. **Опционально, на бэке:** окно снисхождения. Если предъявленный токен погашен меньше N секунд назад **и** его преемник (`replacedByJti`) ещё активен — значит цепочка не разветвлялась, это гонка честного клиента: вернуть 401 без казни семейства. Вот здесь и окупается выбор из 3.3. Дефолт `0` — строгое поведение, чтобы безопасность по умолчанию не размывалась; knob в конфиге на случай, если в проде пойдут ложные срабатывания.

Разница принципиальная: при краже атакующий предъявляет токен из **середины** цепочки, чей преемник давно погашен ротациями легитимного клиента. При гонке предъявляется **последний** обменянный токен, преемник которого активен. Это различимо, и `replacedByJti` — то, чем оно различается.

### 3.6 Судьба `tokenVersion`

Остаётся. Разделение ролей:

- `refresh_tokens` — per-session ротация и отзыв;
- `tokenVersion` — глобальный kill switch: бан, реакция на инцидент, будущая смена пароля. Одно `UPDATE` валит все сессии пользователя без скана таблицы.

Механизмы дополняют друг друга. `tokenVersion` уже реализован и покрыт тестами — ломать нечего.

---

## 4. Код

### 4.1 `RefreshToken`

`src/main/java/io/hexlet/cv/model/RefreshToken.java` (новый файл)

```java
package io.hexlet.cv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Один выданный refresh-токен. Существование активной строки — единственный признак
 * того, что токен ещё не обменивали. Ротация гасит строку и создаёт следующую
 * с тем же familyId.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_family", columnList = "family_id, revoked_at"),
    @Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
    @Index(name = "idx_refresh_tokens_expires", columnList = "expires_at")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    /** Совпадает с claim jti токена. */
    @Id
    private UUID jti;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Идентификатор цепочки ротаций одной сессии. Не меняется при обновлении. */
    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** null — токен активен. Иначе момент отзыва. */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /** jti токена, выданного взамен этого. Даёт проходимую цепочку для аудита и отсечки гонок. */
    @Column(name = "replaced_by_jti")
    private UUID replacedByJti;

    public boolean isActive() {
        return revokedAt == null;
    }
}
```

**Зачем что:**

- **`jti` как `@Id`.** Первичный ключ и есть идентификатор токена — отдельный технический `id` не нужен, а поиск по `jti` становится поиском по PK.
- **Индекс `(family_id, revoked_at)` — составной.** По нему идёт проверка живости семейства на каждом запросе (3.4). Индекс только по `family_id` заставил бы читать строки, чтобы отфильтровать погашенные; составной отвечает из индекса.
- **Индекс по `expires_at`** — для джоба очистки (4.8). Без него ежедневный `DELETE` пойдёт full scan по растущей таблице.
- **snake_case в `columnList`.** Физические имена колонок при дефолтной стратегии Spring — `family_id`, `user_id`. Hibernate 6 логические имена в `columnList` разрешает, но полагаться на это незачем, когда можно написать точно.
- **`userId` как `Long`, а не `@ManyToOne User`.** Сознательно: сущность пишется и читается в горячем пути, тащить за ней граф `User` (у которого коллекции `@OneToMany`) не нужно, а ленивый прокси в валидаторе на каждый запрос — лишний риск. Цена: при `ddl-auto: update` FK-ограничение не создастся, и после удаления пользователя останутся сироты. Их подберёт джоб очистки по `expires_at`; явный FK добавим первой же миграцией Flyway (10.2).

### 4.2 `RefreshTokenRepository`

`src/main/java/io/hexlet/cv/repository/RefreshTokenRepository.java` (новый файл)

```java
package io.hexlet.cv.repository;

import io.hexlet.cv.model.RefreshToken;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Атомарно гасит токен, если он ещё активен.
     * Возвращает 1 — токен был активен и погашен; 0 — не найден либо уже погашен.
     * Единственная точка, отвечающая на вопрос «первое предъявление или повторное».
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update RefreshToken t
              set t.revokedAt = :now, t.replacedByJti = :successorJti
            where t.jti = :jti
              and t.revokedAt is null
           """)
    int revokeIfActive(@Param("jti") UUID jti,
                       @Param("successorJti") UUID successorJti,
                       @Param("now") Instant now);

    /** Гасит всю цепочку: logout одной сессии либо реакция на детект кражи. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update RefreshToken t
              set t.revokedAt = :now
            where t.familyId = :familyId
              and t.revokedAt is null
           """)
    int revokeFamily(@Param("familyId") UUID familyId, @Param("now") Instant now);

    /** «Выйти со всех устройств» / реакция на инцидент. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update RefreshToken t
              set t.revokedAt = :now
            where t.userId = :userId
              and t.revokedAt is null
           """)
    int revokeAllForUser(@Param("userId") Long userId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshToken t where t.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") Instant cutoff);
}
```

**Зачем что:**

- **`revokeIfActive` — атомарный `UPDATE ... WHERE revokedAt IS NULL`, без предварительного чтения.** Это ядро всей схемы. Читать строку, проверять статус в Java и потом писать — значит открыть окно для гонки между чтением и записью и получить два успешных refresh одним токеном. СУБД решает это за нас: возвращённое число строк и есть ответ «был активен или нет». Дополнительный бонус — исчезает различие между «строки нет», «уже погашена» и «кто-то погасил параллельно»: это один и тот же случай, «токен не активен на момент предъявления», и обрабатывать его надо одинаково.
- **`and t.revokedAt is null` в `revokeFamily`/`revokeAllForUser`.** Без фильтра каждый вызов перезаписывал бы уже погашенные строки (лишние записи) и возвращал бы бессмысленное число — а мы это число логируем как «сколько сессий погасили».
- **`deleteExpired` через `@Query`, а не производный `deleteByExpiresAtBefore`.** Производный delete в Spring Data JPA сначала **загружает** сущности, потом удаляет по одной. На таблице такого размера это недопустимо. Явный bulk `delete` — одно выражение.
- **Нет `findByJti`.** Это дубликат унаследованного `findById` — `jti` и есть `@Id`.

### 4.3 `RefreshTokenStore` — транзакционные границы

`src/main/java/io/hexlet/cv/security/RefreshTokenStore.java` (новый файл)

```java
package io.hexlet.cv.security;

import io.hexlet.cv.model.RefreshToken;
import io.hexlet.cv.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Транзакционные границы для refresh_tokens.
 *
 * Вынесено в отдельный бин намеренно. TokenService при детекте повтора обязан
 * сначала закоммитить отзыв семейства, а потом бросить BadCredentialsException.
 * Если бы отзыв и бросок исключения жили в одной транзакции, unchecked-исключение
 * откатило бы UPDATE и отзыв не состоялся бы. Вызов @Transactional-метода изнутри
 * того же класса идёт мимо прокси и от этого не спасает — нужен именно другой бин.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final RefreshTokenRepository repository;

    @Transactional
    public void save(RefreshToken token) {
        repository.save(token);
    }

    /**
     * Одной транзакцией: гасит предъявленный токен и сохраняет преемника.
     * false — предъявленный токен не был активен; преемник в этом случае
     * не сохраняется и наружу не уходит.
     */
    @Transactional
    public boolean rotate(UUID presentedJti, RefreshToken successor, Instant now) {
        if (repository.revokeIfActive(presentedJti, successor.getJti(), now) == 0) {
            return false;
        }
        repository.save(successor);
        return true;
    }

    /** Отдельная транзакция: обязана закоммититься до броска исключения вызывающим. */
    @Transactional
    public int revokeFamily(UUID familyId, Instant now) {
        return repository.revokeFamily(familyId, now);
    }

    @Transactional
    public int revokeAllForUser(Long userId, Instant now) {
        return repository.revokeAllForUser(userId, now);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> find(UUID jti) {
        return repository.findById(jti);
    }

    @Transactional
    public int purgeExpired(Instant cutoff) {
        return repository.deleteExpired(cutoff);
    }
}
```

**Зачем этот класс вообще.** Это не слой ради слоя — это единственное место, где решается, что с чем коммитится вместе:

- `rotate()` — **одна** транзакция: погасить предъявленный и записать преемника. Разорвать нельзя: если преемник не записался, а предъявленный погашен, клиент получит на руки refresh-токен, которого нет в БД, и следующий refresh прочитается как кража.
- `revokeFamily()` — **своя** транзакция, потому что она должна выжить после исключения.
- `refresh()` в `TokenService` — **без** `@Transactional`, чтобы не накрыть всё это одной объемлющей транзакцией и не вернуть ровно ту проблему, от которой ушли.

Альтернатива — `@Transactional(propagation = REQUIRES_NEW)`, но тогда пришлось бы решать проблему self-invocation через self-injection с `@Lazy`, и читателю кода надо держать в голове тонкости распространения транзакций. Отдельный бин честнее.

### 4.4 `JwtProperties` и конфигурация

`src/main/java/io/hexlet/cv/config/JwtProperties.java` — добавить три поля:

```java
@Component
@ConfigurationProperties(prefix = "app.security.jwt")
@Getter
@Setter
public class JwtProperties {
    private long accessTokenValiditySeconds;
    private long refreshTokenValiditySeconds;
    private String issuer;
    private String audience;

    /**
     * Фаза 2 релиза: требовать jti/familyId. Пока false — токены старого формата
     * принимаются и мягко переводятся на новую схему. См. раздел 5.
     */
    private boolean enforceSessionClaims = false;

    /**
     * Окно, в котором повторное предъявление только что обменянного токена
     * считается гонкой честного клиента, а не кражей. 0 — строгое поведение. См. 3.5.
     */
    private long refreshRaceGraceSeconds = 0;

    /** Расписание очистки просроченных строк refresh_tokens. */
    private String cleanupCron = "0 17 3 * * *";
}
```

`src/main/resources/application.yml`:

```yaml
app:
  security:
    jwt:
      access-token-validity-seconds: 900        # 15 минут
      refresh-token-validity-seconds: 2592000   # 30 дней
      issuer: hexlet-cv
      audience: hexlet-cv-frontend
      enforce-session-claims: false             # → true через 30 дней после релиза, см. раздел 5
      refresh-race-grace-seconds: 0             # >0 — не казнить семейство при гонке, см. 3.5
      cleanup-cron: "0 17 3 * * *"
```

Cron `0 17 3 * * *` — 03:17, а не 03:00: ровные значения собирают на себя все джобы всех сервисов, а нам не нужна конкуренция за БД в момент пика фоновых задач.

### 4.5 `JWTUtils`

`src/main/java/io/hexlet/cv/util/JWTUtils.java`

```java
public static final String CLAIM_TYPE = "type";
public static final String CLAIM_TOKEN_VERSION = "tokenVersion";
public static final String CLAIM_FAMILY_ID = "familyId";
public static final String TYPE_REFRESH = "refresh";

public String generateAccessToken(User user, UUID familyId) {
    JwtClaimsSet claims = baseClaims(user, jwtProperties.getAccessTokenValiditySeconds())
            .claim("roles", List.of(user.getRole().name()))
            .claim(CLAIM_FAMILY_ID, familyId.toString())
            .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
}

public String generateRefreshToken(User user, UUID jti, UUID familyId) {
    JwtClaimsSet claims = baseClaims(user, jwtProperties.getRefreshTokenValiditySeconds())
            .id(jti.toString())
            .claim(CLAIM_TYPE, TYPE_REFRESH)
            .claim(CLAIM_FAMILY_ID, familyId.toString())
            .build();
    return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
}

private JwtClaimsSet.Builder baseClaims(User user, long validitySeconds) {
    Instant now = Instant.now();
    return JwtClaimsSet.builder()
            .issuer(jwtProperties.getIssuer())
            .audience(List.of(jwtProperties.getAudience()))
            .issuedAt(now)
            .expiresAt(now.plusSeconds(validitySeconds))
            .subject(user.getEmail())
            .claim(CLAIM_TOKEN_VERSION, user.getTokenVersion());
}
```

**Зачем что:**

- **Принимаем `User`, а не `String username`.** Сейчас каждый генератор сам делает `findByEmail` ([JWTUtils.java:29](../src/main/java/io/hexlet/cv/util/JWTUtils.java#L29), [:44](../src/main/java/io/hexlet/cv/util/JWTUtils.java#L44)). На один refresh это два одинаковых запроса, а с записью в `refresh_tokens` понадобится ещё и `user.getId()` — стало бы три. `TokenService` грузит пользователя один раз и передаёт вниз.
- **Старые перегрузки `generateAccessToken(String)` / `generateRefreshToken(String)` удалить, а не оставить «для совместимости».** Пока живёт перегрузка без `familyId`, любой новый вызов молча выпустит токен, который не проходит проверку семейства, — то есть тихо сломает авторизацию в рантайме. Компилятор должен на это ругаться. Правки затронут 16 вызовов в тестах ([ArticleControllerTest](../src/test/java/io/hexlet/cv/controller/ArticleControllerTest.java), [TeamControllerTest](../src/test/java/io/hexlet/cv/controller/TeamControllerTest.java), [EncodersConfigTest](../src/test/java/io/hexlet/cv/config/EncodersConfigTest.java) и др.) — заводим тестовый хелпер, выдающий пару через `TokenService`.
- **Один `Instant.now()` на токен**, в `baseClaims`. `iat` и `exp` должны считаться от одного чтения часов.
- **Константы вместо строковых литералов.** `"familyId"` появляется в четырёх файлах — генераторе, двух валидаторах и `TokenService`. Опечатка в одном из них даёт не ошибку компиляции, а тихо неработающую проверку сессии.

### 4.6 `TokenService`

`src/main/java/io/hexlet/cv/security/TokenService.java` (переписывается)

```java
package io.hexlet.cv.security;

import io.hexlet.cv.config.JwtProperties;
import io.hexlet.cv.model.RefreshToken;
import io.hexlet.cv.model.User;
import io.hexlet.cv.repository.UserRepository;
import io.hexlet.cv.util.JWTUtils;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

/**
 * Ротация refresh-токенов с детектом повторного использования.
 *
 * @Transactional здесь нет намеренно: отзыв семейства при детекте повтора должен
 * закоммититься до броска BadCredentialsException. Транзакционные границы — в
 * RefreshTokenStore.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final RefreshTokenStore store;

    /** Логин и регистрация: новая сессия — новое семейство. */
    public Tokens authenticateAndGenerate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        return startNewSession(requireUser(email));
    }

    public Tokens refresh(String refreshToken) {
        Jwt jwt = decodeOrReject(refreshToken);
        User user = requireUser(jwt.getSubject());

        UUID presentedJti = parseUuidOrNull(jwt.getId());
        UUID familyId = parseUuidOrNull(jwt.getClaimAsString(JWTUtils.CLAIM_FAMILY_ID));
        if (presentedJti == null || familyId == null) {
            return handleLegacyToken(user);
        }

        Instant now = Instant.now();
        Issued issued = issue(user, familyId, now);

        // Атомарно: погасить предъявленный, записать преемника.
        // false означает «токен не был активен» — либо кража, либо гонка.
        if (store.rotate(presentedJti, issued.record(), now)) {
            return issued.tokens();
        }

        if (isBenignRace(presentedJti, now)) {
            log.info("Concurrent refresh ignored: jti={} familyId={} userId={}",
                    presentedJti, familyId, user.getId());
            throw new BadCredentialsException("Concurrent refresh");
        }

        int revoked = store.revokeFamily(familyId, now);
        log.warn("Refresh token reuse detected: jti={} familyId={} userId={} sessionsRevoked={}",
                presentedJti, familyId, user.getId(), revoked);
        throw new BadCredentialsException("Refresh token already used");
    }

    /** Logout: гасит только предъявленную сессию, остальные устройства остаются живыми. */
    public void revokeByRefreshToken(String refreshToken) {
        UUID familyId = familyIdOrNull(refreshToken);
        if (familyId != null) {
            store.revokeFamily(familyId, Instant.now());
        }
    }

    /** Глобальный отзыв: реакция на инцидент, будущая смена пароля. См. 10.1. */
    public void revokeAllSessions(Long userId) {
        store.revokeAllForUser(userId, Instant.now());
    }

    /**
     * Токен старого формата (выдан до релиза ротации).
     * Фаза 1 — обменять на новую сессию, не разлогинивая человека.
     * Фаза 2 — отклонить. См. раздел 5.
     */
    private Tokens handleLegacyToken(User user) {
        if (jwtProperties.isEnforceSessionClaims()) {
            throw new BadCredentialsException("Refresh token predates rotation");
        }
        log.info("Migrating legacy refresh token to a rotating session: userId={}", user.getId());
        return startNewSession(user);
    }

    private Tokens startNewSession(User user) {
        Issued issued = issue(user, UUID.randomUUID(), Instant.now());
        store.save(issued.record());
        return issued.tokens();
    }

    /**
     * Гонка честного клиента: предъявленный токен погашен только что, и его преемник
     * ещё активен — значит цепочка не разветвлялась. При краже предъявляется токен
     * из середины цепочки, чей преемник давно погашен. См. 3.5.
     */
    private boolean isBenignRace(UUID presentedJti, Instant now) {
        long grace = jwtProperties.getRefreshRaceGraceSeconds();
        if (grace <= 0) {
            return false;
        }
        return store.find(presentedJti)
                .filter(t -> t.getRevokedAt() != null)
                .filter(t -> t.getRevokedAt().isAfter(now.minusSeconds(grace)))
                .map(RefreshToken::getReplacedByJti)
                .flatMap(store::find)
                .filter(RefreshToken::isActive)
                .isPresent();
    }

    private Issued issue(User user, UUID familyId, Instant now) {
        UUID jti = UUID.randomUUID();
        Tokens tokens = new Tokens(
                jwtUtils.generateAccessToken(user, familyId),
                jwtUtils.generateRefreshToken(user, jti, familyId)
        );
        RefreshToken record = RefreshToken.builder()
                .jti(jti)
                .userId(user.getId())
                .familyId(familyId)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtProperties.getRefreshTokenValiditySeconds()))
                .build();
        return new Issued(tokens, record);
    }

    private Jwt decodeOrReject(String refreshToken) {
        try {
            return jwtUtils.decodeRefresh(refreshToken);
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid refresh token", e);
        }
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Unknown token subject"));
    }

    /** null вместо исключения: отсутствующий или битый claim — не 500, а путь к 401. */
    private UUID parseUuidOrNull(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private UUID familyIdOrNull(String refreshToken) {
        try {
            return parseUuidOrNull(jwtUtils.decodeRefresh(refreshToken)
                    .getClaimAsString(JWTUtils.CLAIM_FAMILY_ID));
        } catch (JwtException e) {
            return null;
        }
    }

    private record Issued(Tokens tokens, RefreshToken record) {
    }

    public record Tokens(String access, String refresh) {
    }
}
```

**Зачем что:**

- **`parseUuidOrNull`, а не `UUID.fromString` напрямую.** Это критично. У пользователей на руках refresh-cookie **без** `jti` и `familyId` — maxAge 30 дней ([application.yml:52](../src/main/resources/application.yml#L52)). Прямой `UUID.fromString(jwt.getId())` дал бы `NullPointerException`, а `RefreshController` ловит только `BadCredentialsException` ([RefreshController.java:34](../src/main/java/io/hexlet/cv/controller/RefreshController.java#L34)) — значит **500 вместо 401 на легитимном трафике первые 30 дней после релиза.** Отдельно от `null` надо ловить и `IllegalArgumentException` — на непустой, но битый UUID в claim.
- **`requireUser` бросает `BadCredentialsException`, а не `orElseThrow()`.** Голый `orElseThrow()` даёт `NoSuchElementException` → 500. А случай реальный: удалённый пользователь с живой cookie.
- **`authenticateAndGenerate` без `@Transactional`.** Заворачивать `authenticationManager.authenticate()` в транзакцию значило бы держать соединение с БД всё время проверки bcrypt (~100 мс на запрос логина).
- **Один `Instant now` на всю операцию**, прокинутый в `issue()` и `rotate()`. Момент отзыва предшественника и момент выдачи преемника должны совпадать — иначе в цепочке аудита появятся необъяснимые микрозазоры.
- **Обе «плохие» ветки после `rotate()` возвращают 401, но делают разное.** Гонка — `log.info` и никакого отзыва. Кража — `log.warn` и казнь семейства. Одинаковый ответ клиенту (не подсказываем атакующему), разная реакция сервера.
- **`sessionsRevoked` в логе.** Это не украшение: если число ненулевое, а сессии продолжают работать, — значит отзыв откатывается транзакцией. Диагностический крючок ровно на тот класс ошибок, который описан в 4.3.

### 4.7 `EncodersConfig` и `UserRepository`

Один запрос вместо двух проверок (обоснование в 3.4).

`src/main/java/io/hexlet/cv/repository/UserRepository.java` — добавить:

```java
/**
 * Валидна ли сессия: пользователь существует, tokenVersion совпадает,
 * и в семействе есть хотя бы один неотозванный refresh-токен.
 * Одним запросом — вызывается на каждом запросе с access-cookie.
 */
@Query("""
       select count(u.id) > 0 from User u
        where u.email = :email
          and u.tokenVersion = :tokenVersion
          and exists (select 1 from RefreshToken t
                       where t.familyId = :familyId
                         and t.userId = u.id
                         and t.revokedAt is null)
       """)
boolean isSessionValid(@Param("email") String email,
                       @Param("tokenVersion") long tokenVersion,
                       @Param("familyId") UUID familyId);
```

`and t.userId = u.id` — чтобы `familyId` из чужого токена не проходил проверку. Подделать подписанный JWT нельзя, но привязка стоит одну строку, а инвариант «семейство принадлежит этому пользователю» лучше держать в запросе, чем в предположении.

`src/main/java/io/hexlet/cv/config/EncodersConfig.java`:

```java
@Bean
JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaKeys.getPublicKey()).build();
    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefault(), mustNotBeRefresh(),
            issuerValid(), audienceValid(), accessSessionValid()
    ));
    return decoder;
}

@Bean
JwtDecoder refreshTokenDecoder() {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaKeys.getPublicKey()).build();
    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefault(), mustBeRefresh(), tokenVersionValid(),
            issuerValid(), audienceValid(), refreshClaimsPresent()
    ));
    return decoder;
}

/**
 * Access-токен: tokenVersion (глобальный kill switch) и живость сессии — одним запросом.
 * Заменяет tokenVersionValid() на access-декодере.
 */
private OAuth2TokenValidator<Jwt> accessSessionValid() {
    return jwt -> {
        Long tokenVersion = jwt.getClaim(JWTUtils.CLAIM_TOKEN_VERSION);
        if (tokenVersion == null) {
            return invalid("Missing tokenVersion claim");
        }
        String rawFamilyId = jwt.getClaimAsString(JWTUtils.CLAIM_FAMILY_ID);
        if (rawFamilyId == null) {
            // Фаза 1: токен выпущен до релиза — проверяем только tokenVersion.
            return jwtProperties.isEnforceSessionClaims()
                    ? invalid("Missing familyId claim")
                    : tokenVersionValid().validate(jwt);
        }
        UUID familyId;
        try {
            familyId = UUID.fromString(rawFamilyId);
        } catch (IllegalArgumentException e) {
            return invalid("Malformed familyId claim");
        }
        return userRepository.isSessionValid(jwt.getSubject(), tokenVersion, familyId)
                ? OAuth2TokenValidatorResult.success()
                : invalid("Session revoked");
    };
}

/**
 * Refresh-токен обязан нести jti и familyId. Обязательный валидатор, не опциональный:
 * без него токены старого формата дойдут до TokenService и дадут 500 вместо 401.
 */
private OAuth2TokenValidator<Jwt> refreshClaimsPresent() {
    return jwt -> {
        boolean present = jwt.getId() != null
                && jwt.getClaimAsString(JWTUtils.CLAIM_FAMILY_ID) != null;
        if (present || !jwtProperties.isEnforceSessionClaims()) {
            return OAuth2TokenValidatorResult.success();
        }
        return invalid("Missing jti or familyId");
    };
}

private OAuth2TokenValidatorResult invalid(String message) {
    return OAuth2TokenValidatorResult.failure(new OAuth2Error(INVALID_TOKEN, message, null));
}
```

`tokenVersionValid()` остаётся как есть — он нужен на refresh-декодере и как фаза-1 fallback на access-декодере.

**Почему семейство не проверяется на refresh-декодере.** Там это делает атомарный `rotate()`. Добавить туда `exists`-проверку значило бы вернуть чтение перед записью — ровно ту гонку, от которой мы ушли в 4.2. Валидатор на refresh-декодере проверяет только форму токена, состояние — дело `rotate()`.

### 4.8 Очистка просроченных строк

Это часть **того же** PR, не «следующая итерация». Считаем: access живёт 15 минут → активный пользователь делает ~96 refresh в сутки → 96 строк в сутки на человека. Тысяча активных пользователей — 100k строк в день, ~3 млн в месяц. Джоб — 15 строк кода; откладывать его значит осознанно закладывать деградацию в тот же релиз.

`src/main/java/io/hexlet/cv/security/RefreshTokenCleanupJob.java` (новый файл)

```java
package io.hexlet.cv.security;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Просроченные refresh-токены уже отвергаются декодером — строки только занимают место. */
@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupJob {

    private final RefreshTokenStore store;

    @Scheduled(cron = "${app.security.jwt.cleanup-cron}")
    public void purgeExpired() {
        int removed = store.purgeExpired(Instant.now());
        if (removed > 0) {
            log.info("Purged {} expired refresh tokens", removed);
        }
    }
}
```

`src/main/java/io/hexlet/cv/App.java` — добавить `@EnableScheduling`. **В проекте его сейчас нет** (проверил grep по `src/main`), то есть это новая инфраструктура в приложении, а не бесплатная аннотация: планировщик по умолчанию однопоточный и делит поток со всеми будущими джобами. Упомянуть в описании PR.

Отдельно: первый запуск после долгой работы на фазе 1 может удалять миллионы строк одним `DELETE` — долгая блокировка. Если к моменту включения джоба таблица уже большая, первую очистку сделать руками порциями. Дальше суточный объём ограничен и проблемы нет.

### 4.9 Контроллеры

**`RefreshController`** — при 401 гасить cookie:

```java
} catch (BadCredentialsException e) {
    var expired = tokenCookieService.buildExpiredCookies();
    response.addHeader(HttpHeaders.SET_COOKIE, expired.access().toString());
    response.addHeader(HttpHeaders.SET_COOKIE, expired.refresh().toString());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
}
```

Зачем: если refresh не удался, токены на руках у клиента мертвы окончательно. Пока браузер продолжает их присылать, каждый запрос идёт в валидатор и получает отказ — а из-за `cookieTokenResolver` это даёт 401 **и на публичных страницах** (см. 9.1). Гашение cookie в ответе на неудачный refresh — самая дешёвая точка, где это лечится.

**`LogoutController`** — изменений не требует: `revokeByRefreshToken` сохраняет сигнатуру, меняется только семантика (одна сессия вместо всех). Но это надо назвать в описании PR: поведение logout меняется для пользователя.

---

## 5. Двухфазный релиз (обязательно)

**Проблема.** На момент деплоя у людей на руках cookie без `jti`/`familyId`: refresh — до 30 дней, access — до 15 минут. Если сразу включить строгую проверку, разлогинятся все. Хуже: держатели живой access-cookie получат 401 **на всём сайте, включая публичные страницы** (механика в 9.1), и автоматически восстановиться не смогут — их refresh тоже старого формата.

**Решение — два деплоя, разделённые флагом `enforce-session-claims`.**

**Фаза 1 (`enforce-session-claims: false`), в момент мержа:**

- новые токены несут `jti`/`familyId`, таблица заполняется, ротация с детектом повтора работает для всех новых сессий;
- старый refresh-токен при первом же обновлении **мягко переезжает** на новую схему: `handleLegacyToken` выдаёт новое семейство (4.6). Никто не разлогинивается;
- старый access-токен проверяется только по `tokenVersion` — как сегодня.

Цена фазы 1: украденный **старый** refresh-токен остаётся реиграбельным, пока не переедет. Это в точности статус-кво, то есть не регресс, а отложенное улучшение. Через 30 дней (TTL refresh) старых токенов не остаётся вообще.

**Фаза 2 (`enforce-session-claims: true`), через 30+ дней:** отсутствие `jti`/`familyId` → 401. Отдельного деплоя кода не нужно, только конфиг.

**Почему не «просто разлогинить всех»**: один булев флаг убирает целый класс релизных рисков, а откат при проблемах — это правка конфига, а не revert PR. Если команда сознательно решит разлогинить всех, это должно быть **записанным решением в описании PR**, а не побочным эффектом, обнаруженным в проде.

---

## 6. Требования к фронтенду (блокирующие)

Без этих двух пунктов бэкенд будет технически корректен и практически невыносим.

**6.1 Single-flight на refresh — обязательно.** Один refresh в полёте на весь клиент; параллельные 401 ждут его результата и переиспользуют. Без этого две вкладки, ретрай после обрыва сети или двойной клик приводят к предъявлению одного токена дважды, а это по определению детект кражи → казнь семейства → разлогин с устройства. Механизм 3.5 срабатывает по своим.

Реализация — общий promise: первый 401 запускает refresh и сохраняет promise; остальные подписываются на него, а не создают новый запрос.

**6.2 На неудачный refresh — чистить локальное состояние и продолжать анонимно.** Ответ 401 от `/api/auth/refresh` означает «сессии больше нет». Клиент не должен ретраить и не должен уходить в цикл; он гасит локальное состояние авторизации и рендерит страницу как для анонима. Серверная половина этого — гашение cookie из 4.9.

**6.3 Проверить, что бэкенд-эндпоинты фронта не ходят под чужой сессией.** После включения фазы 2 access-токен без `familyId` перестанет работать; любое место, где токен генерируется в обход `TokenService` (скрипты, фикстуры, интеграции), сломается. На момент написания таких мест нет — `generateAccessToken`/`generateRefreshToken` вне `TokenService` вызываются только в тестах.

---

## 7. Тесты

Помечены обязательные — без них дефекты, ради которых всё это делается, воспроизводимо проходят ревью.

**7.1 (обязательный) Replay действительно гасит семейство.**
Логин → refresh (получаем `R2`) → повторно предъявляем `R1` → 401 → **предъявляем `R2` и тоже ждём 401.**

Последний шаг — весь смысл теста. Проверка «replay даёт 401» проходит и в том случае, когда отзыв семейства откатился транзакцией: код возврата тот же, а семейство живое. Это самый важный тест во всём PR — он единственный отличает работающий детект кражи от детекта, который пишет в лог и ничего не делает (механика в 4.3).

**7.2 (обязательный) Токен старого формата → 401, не 500.**
Собрать refresh-токен без `jti`/`familyId` (старым набором claims), включить `enforce-session-claims: true`, `POST /api/auth/refresh` → ровно 401. Ловит `NullPointerException` из 4.6.

**7.3 (обязательный) Access-токен мёртв сразу после logout.**
Логин → `GET /account/knowledge` = 2xx → `POST /users/sign_out` → тот же access-токен → 401. Ловит регресс из 3.4 (иначе токен живёт до 15 минут).

**7.4 Logout гасит только одну сессию.** Два логина (два семейства) → logout по первому → access и refresh второго живы. Это и есть заявленное улучшение против сегодняшнего поведения.

**7.5 Детект кражи убивает и access-токены.** Логин → refresh → replay → access-токен из последней ротации тоже 401.

**7.6 Обычная цепочка ротаций.** Три refresh подряд: каждый успешен, каждый следующий токен отличается от предыдущего, `familyId` не меняется.

**7.7 Конкурентный refresh одним токеном.** Два потока, один токен: ровно один успех, ровно один 401.

**7.8 Окно снисхождения.** При `refresh-race-grace-seconds > 0` повторное предъявление только что обменянного токена даёт 401, **но** семейство остаётся живым. При `= 0` — семейство гаснет.

**7.9 Фаза 1 мягко переводит старый токен.** `enforce-session-claims: false`, refresh-токен старого формата → 200, в ответе новая пара, в `refresh_tokens` появилось семейство.

**7.10 `tokenVersion` остался глобальным kill switch.** Два семейства у одного пользователя → `incrementTokenVersion` → оба мертвы. Регрессионный тест на 3.6: иначе при следующем рефакторинге его тихо потеряют.

**7.11 Джоб очистки** удаляет просроченные строки и не трогает активные.

Тесты 7.1–7.5 писать в стиле существующего [RefreshControllerTest](../src/test/java/io/hexlet/cv/controller/RefreshControllerTest.java) — через `MockMvc` и cookie, **не на моках репозитория**. Дефект из 4.3 — это поведение транзакции; на моках он невидим, тест будет зелёным на сломанном коде.

---

## 8. Чеклист приёмки PR

- [ ] `RefreshToken`, `RefreshTokenRepository`, `RefreshTokenStore`, `RefreshTokenCleanupJob` добавлены
- [ ] `refresh()` не помечен `@Transactional`; `revokeFamily` вызывается через отдельный бин
- [ ] `revokeIfActive` — атомарный `UPDATE`, без предварительного чтения
- [ ] `jti` + `familyId` в refresh-токене, `familyId` — и в access-токене
- [ ] старые перегрузки `generate*Token(String)` удалены, вызовы в тестах переведены
- [ ] `accessSessionValid()` на access-декодере, `refreshClaimsPresent()` на refresh-декодере
- [ ] парсинг UUID из claims не может дать 500
- [ ] `RefreshController` гасит cookie при 401
- [ ] `@EnableScheduling` в `App`, cron в конфиге
- [ ] `enforce-session-claims: false` на момент мержа
- [ ] тесты 7.1, 7.2, 7.3 присутствуют и падают на коде без соответствующих правок (проверить руками!)
- [ ] checkstyle зелёный
- [ ] в описании PR: смена семантики logout, требование single-flight на фронте, план фазы 2 с датой
- [ ] заведены задачи из раздела 10

---

## 9. Что ревьюер упустил или можно усилить

Замечание @arthur810629 корректное и по существу. Ниже то, что в него не попало и что стоит добавить в задачу.

### 9.1 Невалидная access-cookie даёт 401 на публичных страницах — и баг уже в проде

`cookieTokenResolver` ([SecurityConfig.java:134-147](../src/main/java/io/hexlet/cv/config/SecurityConfig.java#L134-L147)) отдаёт `access_token` из cookie на **каждом** запросе, где cookie присутствует, независимо от того, требует ли маршрут авторизации. В Spring Security ошибка аутентификации короткозамыкает цепочку **до** проверки авторизации — то есть невалидный токен даёт 401 даже там, где стоит `permitAll()` ([SecurityConfig.java:57](../src/main/java/io/hexlet/cv/config/SecurityConfig.java#L57)).

Это воспроизводится **уже сейчас**, без всяких правок: пользователь разлогинился на другом устройстве → `tokenVersion` инкрементнулся → его access-cookie стала невалидной → **весь публичный сайт отвечает 401**, пока cookie не истечёт. Для Inertia-приложения, где публичные страницы — это основная часть, это заметно.

Этот PR проблему не создаёт, но расширяет площадь: появляется второй способ сделать access-токен невалидным (отзыв семейства). Минимум — 4.9 и 6.2 (гасить cookie при неудачном refresh). Правильное лечение — отдельная задача: невалидный токен на `permitAll`-маршруте должен приводить к анонимному запросу, а не к 401.

### 9.2 Смена семантики logout — это пользовательское изменение, а не рефакторинг

Сегодня logout выкидывает со всех устройств. После PR — только с текущего. Это ровно то, чего мы хотели, но это **изменение поведения продукта**, а не внутренняя чистка. Оно должно быть в описании PR и, если у проекта есть текст в UI («вы вышли»), возможно, потребует отдельной кнопки «выйти со всех устройств» — под неё как раз есть `revokeAllSessions` (4.6).

### 9.3 Релизный риск не сводится к «все разлогинятся»

Обсуждение задачи молчит про то, что происходит с уже выданными токенами. Их два вида с разными TTL (15 минут и 30 дней), и ломаются они по-разному (9.1). Отсюда раздел 5 — двухфазный релиз. Это не перестраховка: без флага откат при проблемах означает revert PR и повторный разлогин всех при следующей попытке.

### 9.4 `Path` у cookie — незакрытый подпункт той же задачи #1215

В задаче написано «ограниченный Path», в конфиге у обеих cookie `path: /` ([application-prod.yml:19](../src/main/resources/application-prod.yml#L19), [:25](../src/main/resources/application-prod.yml#L25)). Refresh-токен уходит на сервер при каждом запросе к сайту, хотя нужен ровно двум эндпоинтам — `/api/auth/refresh` и `/users/sign_out`.

Это ослабляет всё остальное: чем чаще refresh-токен ходит по сети, тем больше поверхность его утечки, а именно его утечку мы этим PR и пытаемся обнаруживать. Мешает сделать сразу то, что эндпоинты живут под разными префиксами — logout пришлось бы перенести под `/api/auth/`. Отдельная задача, но её стоит связать с #1215, потому что подпункт формально не закрыт.

### 9.5 Ограничения по количеству сессий нет

Ничто не ограничивает число одновременных семейств на пользователя. Автоматизированный логин в цикле создаёт по строке за запрос. Не срочно, но: лимит (например, 5 активных семейств, при превышении гасить самое старое) — это и защита от разрастания таблицы, и функция «список ваших устройств», которую обычно просят следом.

### 9.6 Метрики важнее, чем кажется

`log.warn` при детекте повтора (4.6) годится для расследования конкретного инцидента, но не отвечает на вопрос «а вообще сколько их?». Счётчик детектов в Micrometer (actuator в проекте уже есть — `springBootStarterActuator`) стоит одну строку и даёт две разные вещи: всплеск = кампания по угону сессий, ровный ненулевой фон = у нас ложные срабатывания и надо включать `refresh-race-grace-seconds`. Без метрики отличить одно от другого не получится.

### 9.7 Что проверено и претензий не имеет

Чтобы не искали повторно: RSA-ключи **не** в репозитории (`git ls-files src/main/resources/certs/` пусто, `*.pem` и `certs/` в `.gitignore`), алгоритм зафиксирован через `withPublicKey` (RS256, `alg` из заголовка не выбирается), `exp`/`iss`/`aud` проверяются. Соответствующие подпункты #1215 действительно закрыты.

---

## 10. Что остаётся открытым в #1215 после этого PR

### 10.1 Отзыв при смене пароля — закрыть нельзя

Задача требует отзыва при смене пароля, но флоу смены пароля в проекте нет: `CustomUserDetailsService.changePassword` — заглушка, бросающая `UnsupportedOperationException` ([CustomUserDetailsService.java:39-41](../src/main/java/io/hexlet/cv/service/CustomUserDetailsService.java#L39-L41)). Поля `resetPasswordToken` / `resetPasswordSentAt` в `User` есть ([User.java:55-56](../src/main/java/io/hexlet/cv/model/User.java#L55-L56)), то есть флоу планировался, но не реализован.

Честная позиция: `revokeAllSessions(userId)` в этом PR — **подготовленный крючок без вызова**. Это стоит назвать вслух в описании PR (иначе прилетит и от ревьюера, и от SonarQube как неиспользуемый метод), а подпункт #1215 оставить открытым со ссылкой на задачу про смену пароля. Когда флоу появится, вызов будет одной строкой — но приписывать себе закрытие пункта до этого нельзя.

### 10.2 Миграции схемы

`ddl-auto: update` ([application.yml:26](../src/main/resources/application.yml#L26)) для MVP приемлем, и `refresh_tokens` через него накатится. Но это уже **вторая** таблица, заезжающая так, и у неё три индекса и FK, которого нет (4.1). `ddl-auto: update` **не удаляет и не изменяет** существующие колонки и индексы — значит первая же правка схемы придёт руками по прод-базе.

Flyway или Liquibase надо ставить в план на ближайший спринт, а не «в бэклог». Первая миграция: FK `refresh_tokens.user_id → users.id ON DELETE CASCADE`.

### 10.3 `Path` у cookie

См. 9.4. Отдельная задача, связать с #1215.

---

## 11. Резюме

| Что | Решение |
|---|---|
| Замечание ревьюера | Верно, проверено по коду. Пункт «ротация» закрывать нельзя |
| Схема | `jti` + `familyId` + таблица `refresh_tokens`, детект повторного использования, отзыв семейства |
| Состояние строки | `revokedAt` (nullable) + `replacedByJti`, не enum |
| Атомарность | `UPDATE ... WHERE revoked_at IS NULL`, без чтения перед записью |
| Транзакции | Границы в `RefreshTokenStore`; `refresh()` — не транзакционный, иначе отзыв откатится |
| `tokenVersion` | Остаётся глобальным kill switch, роли разделены |
| Access-токен | Несёт `familyId`; одна проверка одним запросом — logout без 15-минутного окна |
| Очистка таблицы | В этот же PR |
| Релиз | Две фазы через `enforce-session-claims`, никто не разлогинивается |
| Фронтенд | Single-flight обязателен, иначе детект стреляет по своим |
| Обязательные тесты | 7.1, 7.2, 7.3 |
| Открыто в #1215 | Отзыв при смене пароля (флоу не существует), `Path` у cookie |
