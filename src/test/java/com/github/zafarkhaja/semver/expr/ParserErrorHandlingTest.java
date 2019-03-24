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

import com.github.zafarkhaja.semver.compiling.UnexpectedTokenException;
import com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 *
 * @author Zafar Khaja &lt;zafarkhaja@gmail.com&gt;
 */
public class ParserErrorHandlingTest {

    @ParameterizedTest
    @MethodSource("parameters")
    public void shouldCorrectlyHandleParseErrors(String invalidExpr, ExprToken unexpected, ExprToken.Type[] expected) {
        try {
            ExpressionParser.newInstance().parse(invalidExpr);
        } catch (UnexpectedTokenException e) {
            assertEquals(unexpected, e.unexpectedToken);
            assertArrayEquals(expected, e.getExpectedTokenTypes());
            return;
        }
        fail("Uncaught exception");
    }
    public static Stream<Arguments> parameters() {
        return Stream.of(
            arguments( "1)",           new ExprToken(RIGHT_PAREN, ")", 1),  new ExprToken.Type[] { EOI } ),
            arguments( "(>1.0.1",      new ExprToken(EOI, null, 7),         new ExprToken.Type[] { RIGHT_PAREN } ),
            arguments( "((>=1 & <2)",  new ExprToken(EOI, null, 11),        new ExprToken.Type[] { RIGHT_PAREN } ),
            arguments( ">=1.0.0 &",    new ExprToken(EOI, null, 9),         new ExprToken.Type[] { NUMERIC } ),
            arguments( "(>2.0 |)",     new ExprToken(RIGHT_PAREN, ")", 7),  new ExprToken.Type[] { NUMERIC } ),
            arguments( "& 1.2",        new ExprToken(AND, "&", 0),          new ExprToken.Type[] { NUMERIC } )
        );
    }
}
