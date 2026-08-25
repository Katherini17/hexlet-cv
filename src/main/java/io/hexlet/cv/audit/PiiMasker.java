package io.hexlet.cv.audit;

import java.text.BreakIterator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Маскирует email перед записью в аудит-лог.
 * Поддерживает национальные адреса (RFC 6531 для локальной части, RFC 5890 для домена):
 * проверка идёт не по списку разрешённых ASCII-символов, а по списку опасных категорий Unicode,
 * поэтому буквы любого письма проходят, а управляющие и невидимые символы отсекаются.
 */
public final class PiiMasker {
    private static final String EMPTY_STRING = "-";
    private static final String MASK = "***";
    private static final int MAX_VISIBLE_GRAPHEMES = 2;

    private PiiMasker() {
    }

    public static String maskEmail(String input) {
        if (input == null || input.isBlank()) {
            return EMPTY_STRING;
        }
        var normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFC);
        if (normalized.codePoints().anyMatch(PiiMasker::isUnsafeCodePoint)) {
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
     * Проверяет, что code point нельзя писать в лог ни в каком виде.
     * Отсекаются управляющие символы и разделители строк (подделка строк лога),
     * форматирующие символы - zero-width и bidi-override (визуальная подмена адреса),
     * пробельные разделители включая NBSP, одиночные суррогаты, private use и неназначенные
     * code point. Буквы, цифры и комбинирующие знаки любого письма считаются безопасными.
     *
     * @param codePoint проверяемый code point
     * @return true, если такой символ нельзя писать в лог
     */
    private static boolean isUnsafeCodePoint(int codePoint) {
        return switch (Character.getType(codePoint)) {
            case Character.CONTROL, Character.FORMAT, Character.SURROGATE, Character.PRIVATE_USE,
                 Character.UNASSIGNED, Character.SPACE_SEPARATOR, Character.LINE_SEPARATOR,
                 Character.PARAGRAPH_SEPARATOR -> true;
            default -> false;
        };
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
        var boundaries = new ArrayList<Integer>();
        for (int end = iterator.next(); end != BreakIterator.DONE; end = iterator.next()) {
            boundaries.add(end);
        }
        int visible = Math.min(MAX_VISIBLE_GRAPHEMES, boundaries.size() - 1);
        if (visible <= 0) {
            return MASK;
        }
        return localPart.substring(0, boundaries.get(visible - 1)) + MASK;
    }
}
