/*
 * The MIT License
 *
 * Copyright 2012-2016 Zafar Khaja <zafarkhaja@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.zafarkhaja.semver;

import static com.github.zafarkhaja.semver.VersionParser.CharType.DIGIT;
import static com.github.zafarkhaja.semver.VersionParser.CharType.EOI;
import static com.github.zafarkhaja.semver.VersionParser.CharType.HYPHEN;
import static com.github.zafarkhaja.semver.VersionParser.CharType.LETTER;
import static com.github.zafarkhaja.semver.VersionParser.CharType.PLUS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.github.zafarkhaja.semver.VersionParser.CharType;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Expression Parser Error Handling tests.
 *
 * @author Zafar Khaja &lt;zafarkhaja@gmail.com&gt;
 */
public class ParserErrorHandlingTest {

  /**
   * Test params.
   *
   * @return test parameters
   */
  public static Stream<Arguments> parameters() {
    return Stream.of(
        arguments("1 ", ' ', 1, new CharType[] {HYPHEN, PLUS, EOI}),
        arguments("1.", null, 2, new CharType[] {DIGIT}),
        arguments("1.2.", null, 4, new CharType[] {DIGIT}),
        arguments("a.b.c", 'a', 0, new CharType[] {DIGIT}),
        arguments("1.b.c", 'b', 2, new CharType[] {DIGIT}),
        arguments("1.2.c", 'c', 4, new CharType[] {DIGIT}),
        arguments("!.2.3", '!', 0, new CharType[] {DIGIT}),
        arguments("1.!.3", '!', 2, new CharType[] {DIGIT}),
        arguments("1.2.!", '!', 4, new CharType[] {DIGIT}),
        arguments("v1.2.3", 'v', 0, new CharType[] {DIGIT}),
        arguments("1.2.3-", null, 6, new CharType[] {DIGIT, LETTER, HYPHEN}),
        arguments("1.2. 3", ' ', 4, new CharType[] {DIGIT}),
        arguments("1.2.3=alpha", '=', 5, new CharType[] {HYPHEN, PLUS, EOI}),
        arguments("1.2.3~beta", '~', 5, new CharType[] {HYPHEN, PLUS, EOI}),
        arguments("1.2.3-be$ta", '$', 8, new CharType[] {PLUS, EOI}),
        arguments("1.2.3+b1+b2", '+', 8, new CharType[] {EOI}),
        arguments("1.2.3-rc!", '!', 8, new CharType[] {PLUS, EOI}),
        arguments("1.2.3-+", '+', 6, new CharType[] {DIGIT, LETTER, HYPHEN}),
        arguments("1.2.3-@", '@', 6, new CharType[] {DIGIT, LETTER, HYPHEN}),
        arguments("1.2.3+@", '@', 6, new CharType[] {DIGIT, LETTER, HYPHEN}),
        arguments("1.2.3-rc.", null, 9, new CharType[] {DIGIT, LETTER, HYPHEN}),
        arguments("1.2.3+b.", null, 8, new CharType[] {DIGIT, LETTER, HYPHEN}),
        arguments("1.2.3-b.+b", '+', 8, new CharType[] {DIGIT, LETTER, HYPHEN}),
        arguments("1.2.3-rc..", '.', 9, new CharType[] {DIGIT, LETTER, HYPHEN}),
        arguments("1.2.3-a+b..", '.', 10, new CharType[] {DIGIT, LETTER, HYPHEN})
    );
  }

  /**
   * A parse error handling test.
   * @param invalidVersion from params[0]
   * @param unexpected from params[1]
   * @param position from params[2]
   * @param expected from params[3]
   */
  @ParameterizedTest
  @MethodSource("parameters")
  public void shouldCorrectlyHandleParseErrors(String invalidVersion, Character unexpected,
                                               int position, CharType[] expected) {
    try {
      VersionParser.parseValidSemVer(invalidVersion);
    } catch (UnexpectedCharacterException e) {
      assertEquals(unexpected, e.getUnexpectedCharacter());
      assertEquals(position, e.getPosition());
      assertArrayEquals(expected, e.getExpectedCharTypes());
      return;
    } catch (ParseException e) {
      if (e.getCause() != null) {
        UnexpectedCharacterException cause = (UnexpectedCharacterException) e.getCause();
        assertEquals(unexpected, cause.getUnexpectedCharacter());
        assertEquals(position, cause.getPosition());
        assertArrayEquals(expected, cause.getExpectedCharTypes());
      }
      return;
    }
    fail("Uncaught exception");
  }
}
