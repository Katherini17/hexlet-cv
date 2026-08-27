package io.hexlet.cv.audit;

/**
 * Проверяет, что значение можно писать в строку журнала.
 */
public final class LogFieldSanitizer {

    private LogFieldSanitizer() {
    }

    /**
     * Проверяет, что в строке нет символов, ломающих или подделывающих запись журнала.
     *
     * @param value проверяемое значение, может быть null
     * @return true, если строку можно писать в лог как есть
     */
    public static boolean isSafe(String value) {
        return value != null && value.codePoints().noneMatch(LogFieldSanitizer::isUnsafeCodePoint);
    }

    /**
     * Проверяет, что code point нельзя писать в лог ни в каком виде.
     * Отсекаются управляющие символы и разделители строк (подделка строк лога),
     * форматирующие символы - zero-width и bidi-override (визуальная подмена значения),
     * пробельные разделители включая NBSP, одиночные суррогаты, private use и неназначенные
     * code point. Буквы, цифры и комбинирующие знаки любого письма считаются безопасными.
     *
     * <p>Пробельные разделители отсекаются ещё и потому, что разбор строки журнала
     * идёт по пробелу: значение с пробелом внутри сдвинуло бы все последующие поля.
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
}
