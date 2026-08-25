package io.hexlet.cv.audit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PiiMaskerTest {

    private static final String MASK = "***";
    private static final String EMPTY_STRING = "-";

    static Stream<Arguments> maskedAddresses() {
        return Stream.of(
                Arguments.of("обычный адрес", "ivan.petrov@example.com", "iv***@example.com"),
                Arguments.of("локальная часть из 1 символа", "a@example.com", "***@example.com"),
                Arguments.of("локальная часть из 2 символов", "ab@example.com", "a***@example.com"),
                Arguments.of("плюс-адресация", "user+tag@example.com", "us***@example.com"),
                Arguments.of("пробелы по краям", "  user@example.com  ", "us***@example.com"),
                Arguments.of("кириллица", "остап@рога-и-копыта.рф", "ос***@рога-и-копыта.рф"),
                Arguments.of("китайский", "用户@例子.测试", "用***@例子.测试"),
                Arguments.of("умлаут в NFC", "schön@beispiel.de", "sc***@beispiel.de"),
                // Тот же адрес в NFD: o + U+0308. После нормализации результат должен совпасть с NFC
                Arguments.of("умлаут в NFD", "scho\u0308n@beispiel.de", "sc***@beispiel.de"),
                // स्ते - один графемный кластер, обрезка не должна его разрубить
                Arguments.of("деванагари", "नमस्ते@भारत.भारत", "नम***@भारत.भारत"),
                // эмодзи вне BMP: суррогатная пара должна остаться целой
                Arguments.of("эмодзи", "😀user@mail.ru", "😀u***@mail.ru")
        );
    }

    static Stream<Arguments> unsafeAddresses() {
        return Stream.of(
                Arguments.of("строка без @", "admin"),
                Arguments.of("перевод строки - подделка строк лога", "us\ner@example.com"),
                Arguments.of("табуляция", "us\ter@example.com"),
                Arguments.of("разделитель строк U+2028", "us\u2028er@example.com"),
                Arguments.of("обычный пробел внутри", "user name@example.com"),
                Arguments.of("неразрывный пробел U+00A0", "user\u00A0name@example.com"),
                Arguments.of("идеографический пробел U+3000", "user\u3000name@example.com"),
                Arguments.of("bidi-override U+202E", "user\u202Eexample@mail.ru"),
                Arguments.of("zero-width space U+200B", "user\u200Bname@example.com"),
                Arguments.of("zero-width joiner U+200D", "user\u200Dname@example.com"),
                Arguments.of("одиночный суррогат", "user\uD83Dname@example.com"),
                Arguments.of("private use U+E000", "user\uE000name@example.com"),
                Arguments.of("два символа @", "user@@example.com"),
                Arguments.of("@ первым символом", "@example.com"),
                Arguments.of("@ последним символом", "user@"),
                // JWT в слоте субъекта: в base64url нет '@', поэтому адрес не распознаётся
                Arguments.of("jwt вместо субъекта", "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ4In0.c2ln")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("maskedAddresses")
    void shouldMaskPartiallyKeepingNationalCharacters(String name, String input, String expected) {
        assertThat(PiiMasker.maskEmail(input)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("maskedAddresses")
    void shouldNeverRevealWholeLocalPart(String name, String input) {
        var trimmed = input.trim();
        var localPart = trimmed.substring(0, trimmed.indexOf('@'));

        var masked = PiiMasker.maskEmail(input);
        var maskedLocalPart = masked.substring(0, masked.indexOf('@'));

        assertThat(maskedLocalPart).doesNotContain(localPart);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unsafeAddresses")
    void shouldMaskCompletelyWhenStringIsUnsafeOrMalformed(String name, String input) {
        assertThat(PiiMasker.maskEmail(input)).isEqualTo(MASK);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t\n"})
    void shouldReturnPlaceholderWhenInputIsEmpty(String input) {
        assertThat(PiiMasker.maskEmail(input)).isEqualTo(EMPTY_STRING);
    }
}
