package io.hexlet.cv.audit;

import java.text.BreakIterator;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * Маскирует email перед записью в аудит-лог.
 * Поддерживает национальные адреса (RFC 6531 для локальной части, RFC 5890 для домена):
 * проверка идёт не по списку разрешённых ASCII-символов, а по списку опасных категорий Unicode,
 * поэтому буквы любого письма проходят, а управляющие и невидимые символы отсекаются.
 */
public final class PiiMasker {
    private static final String MASK = "***";

    private PiiMasker() {
    }

    /**
     * Маскирует адрес. На любом непустом входе возвращает непустой результат, а на входе,
     * непохожем на email, - одну маску. Решение "субъекта нет" сюда не входит:
     * подстановкой плейсхолдера занимается AuditLogger.
     *
     * @param input исходное значение, не null
     * @return замаскированный адрес либо одна маска
     */
    public static String maskEmail(String input) {
        Objects.requireNonNull(input, "Отсутствие субъекта обрабатывает AuditLogger, а не маскировщик");
        var normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFC);
        if (normalized.isEmpty() || !LogFieldSanitizer.isSafe(normalized)) {
            return MASK;
        }
        int domainSepPos = normalized.indexOf('@');
        if (domainSepPos < 1
                || domainSepPos == normalized.length() - 1
                || domainSepPos != normalized.lastIndexOf('@')) {
            return MASK;
        }
        var localPart = normalized.substring(0, domainSepPos);
        var domainPart = normalized.substring(domainSepPos + 1);
        return maskLocalPart(localPart) + "@" + domainPart;
    }

    /**
     * Открывает начало локальной части и закрывает остаток маской.
     * Открывается не больше двух графем и всегда на одну графему меньше, чем есть в строке:
     * иначе для короткого адреса маскировать становится нечего и в лог попадает весь адрес.
     * Обрезка по графемам, а не по char или code point, не разрывает суррогатные пары
     * и последовательности "буква + комбинирующий знак".
     *
     * @param localPart локальная часть адреса, гарантированно непустая
     * @return начало строки, дополненное маской, либо одна маска, если открывать нечего
     */
    private static String maskLocalPart(String localPart) {
        var iterator = BreakIterator.getCharacterInstance(Locale.ROOT);
        iterator.setText(localPart);

        int first = iterator.next();
        int second = iterator.next();
        if (second == BreakIterator.DONE) {
            return MASK;
        }
        int third = iterator.next();
        return localPart.substring(0, third == BreakIterator.DONE ? first : second) + MASK;
    }
}
