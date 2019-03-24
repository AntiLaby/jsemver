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

import com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zafar Khaja &lt;zafarkhaja@gmail.com&gt;
 */
public class LexerTokenTest {
    @Nested
    public static class EqualsMethodTest {

        @Test
        public void shouldBeReflexive() {
            ExprToken token = new ExprToken(NUMERIC, "1", 0);
            assertTrue(token.equals(token));
        }

        @Test
        public void shouldBeSymmetric() {
            ExprToken t1 = new ExprToken(EQUAL, "=", 0);
            ExprToken t2 = new ExprToken(EQUAL, "=", 0);
            assertTrue(t1.equals(t2));
            assertTrue(t2.equals(t1));
        }

        @Test
        public void shouldBeTransitive() {
            ExprToken t1 = new ExprToken(GREATER, ">", 0);
            ExprToken t2 = new ExprToken(GREATER, ">", 0);
            ExprToken t3 = new ExprToken(GREATER, ">", 0);
            assertTrue(t1.equals(t2));
            assertTrue(t2.equals(t3));
            assertTrue(t1.equals(t3));
        }

        @Test
        public void shouldBeConsistent() {
            ExprToken t1 = new ExprToken(HYPHEN, "-", 0);
            ExprToken t2 = new ExprToken(HYPHEN, "-", 0);
            assertTrue(t1.equals(t2));
            assertTrue(t1.equals(t2));
            assertTrue(t1.equals(t2));
        }

        @Test
        public void shouldReturnFalseIfOtherVersionIsOfDifferentType() {
            ExprToken t1 = new ExprToken(DOT, ".", 0);
            assertFalse(t1.equals("."));
        }

        @Test
        public void shouldReturnFalseIfOtherVersionIsNull() {
            ExprToken t1 = new ExprToken(AND, "&", 0);
            ExprToken t2 = null;
            assertFalse(t1.equals(t2));
        }

        @Test
        public void shouldReturnFalseIfTypesAreDifferent() {
            ExprToken t1 = new ExprToken(EQUAL, "=", 0);
            ExprToken t2 = new ExprToken(NOT_EQUAL, "!=", 0);
            assertFalse(t1.equals(t2));
        }

        @Test
        public void shouldReturnFalseIfLexemesAreDifferent() {
            ExprToken t1 = new ExprToken(NUMERIC, "1", 0);
            ExprToken t2 = new ExprToken(NUMERIC, "2", 0);
            assertFalse(t1.equals(t2));
        }

        @Test
        public void shouldReturnFalseIfPositionsAreDifferent() {
            ExprToken t1 = new ExprToken(NUMERIC, "1", 1);
            ExprToken t2 = new ExprToken(NUMERIC, "1", 2);
            assertFalse(t1.equals(t2));
        }
    }
    @Nested
    public static class HashCodeMethodTest {

        @Test
        public void shouldReturnSameHashCodeIfTokensAreEqual() {
            ExprToken t1 = new ExprToken(NUMERIC, "1", 0);
            ExprToken t2 = new ExprToken(NUMERIC, "1", 0);
            assertTrue(t1.equals(t2));
            assertEquals(t1.hashCode(), t2.hashCode());
        }
    }
}
