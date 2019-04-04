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

package com.github.zafarkhaja.semver.expr;

import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.DOT;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.EOI;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.GREATER;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.NUMERIC;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.zafarkhaja.semver.compiling.LexerException;
import com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken;
import com.github.zafarkhaja.semver.util.Stream;
import org.junit.jupiter.api.Test;

/**
 * Lexer tests.
 *
 * @author Zafar Khaja &lt;zafarkhaja@gmail.com&gt;
 */
public class LexerTest {

  @Test
  public void shouldTokenizeVersionString() {
    ExprToken[] expected = {
        new ExprToken(GREATER, ">", 0),
        new ExprToken(NUMERIC, "1", 1),
        new ExprToken(DOT, ".", 2),
        new ExprToken(NUMERIC, "0", 3),
        new ExprToken(DOT, ".", 4),
        new ExprToken(NUMERIC, "0", 5),
        new ExprToken(EOI, null, 6),
    };
    ExprLexer lexer = new ExprLexer();
    Stream<ExprToken> stream = lexer.tokenize(">1.0.0");
    assertArrayEquals(expected, stream.toArray());
  }

  @Test
  public void shouldSkipWhitespaces() {
    ExprToken[] expected = {
        new ExprToken(GREATER, ">", 0),
        new ExprToken(NUMERIC, "1", 2),
        new ExprToken(EOI, null, 3),
    };
    ExprLexer lexer = new ExprLexer();
    Stream<ExprToken> stream = lexer.tokenize("> 1");
    assertArrayEquals(expected, stream.toArray());
  }

  @Test
  public void shouldEndWithEol() {
    ExprToken[] expected = {
        new ExprToken(NUMERIC, "1", 0),
        new ExprToken(DOT, ".", 1),
        new ExprToken(NUMERIC, "2", 2),
        new ExprToken(DOT, ".", 3),
        new ExprToken(NUMERIC, "3", 4),
        new ExprToken(EOI, null, 5),
    };
    ExprLexer lexer = new ExprLexer();
    Stream<ExprToken> stream = lexer.tokenize("1.2.3");
    assertArrayEquals(expected, stream.toArray());
  }

  @Test
  public void shouldRaiseErrorOnIllegalCharacter() {
    ExprLexer lexer = new ExprLexer();
    try {
      lexer.tokenize("@1.0.0");
    } catch (LexerException e) {
      return;
    }
    fail("Should raise error on illegal character");
  }
}
