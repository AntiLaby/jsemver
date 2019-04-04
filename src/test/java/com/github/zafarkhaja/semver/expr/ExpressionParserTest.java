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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.zafarkhaja.semver.Version;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

/**
 * Version Parser tests.
 *
 * @author Zafar Khaja &lt;zafarkhaja@gmail.com&gt;
 */
public class ExpressionParserTest {

  @Test
  public void shouldParseEqualComparisonRange() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> eq = parser.parse("=1.0.0");
    assertTrue(eq.test(Version.valueOf("1.0.0")));
  }

  @Test
  public void shouldParseEqualComparisonRangeIfOnlyFullVersionGiven() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> eq = parser.parse("1.0.0");
    assertTrue(eq.test(Version.valueOf("1.0.0")));
  }

  @Test
  public void shouldParseNotEqualComparisonRange() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> ne = parser.parse("!=1.0.0");
    assertTrue(ne.test(Version.valueOf("1.2.3")));
  }

  @Test
  public void shouldParseGreaterComparisonRange() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> gt = parser.parse(">1.0.0");
    assertTrue(gt.test(Version.valueOf("1.2.3")));
  }

  @Test
  public void shouldParseGreaterOrEqualComparisonRange() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> ge = parser.parse(">=1.0.0");
    assertTrue(ge.test(Version.valueOf("1.0.0")));
    assertTrue(ge.test(Version.valueOf("1.2.3")));
  }

  @Test
  public void shouldParseLessComparisonRange() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> lt = parser.parse("<1.2.3");
    assertTrue(lt.test(Version.valueOf("1.0.0")));
  }

  @Test
  public void shouldParseLessOrEqualComparisonRange() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> le = parser.parse("<=1.2.3");
    assertTrue(le.test(Version.valueOf("1.0.0")));
    assertTrue(le.test(Version.valueOf("1.2.3")));
  }

  @Test
  public void shouldParseTildeRange() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> expr1 = parser.parse("~1");
    assertTrue(expr1.test(Version.valueOf("1.2.3")));
    assertFalse(expr1.test(Version.valueOf("3.2.1")));
    Predicate<Version> expr2 = parser.parse("~1.2");
    assertTrue(expr2.test(Version.valueOf("1.2.3")));
    assertFalse(expr2.test(Version.valueOf("2.0.0")));
    Predicate<Version> expr3 = parser.parse("~1.2.3");
    assertTrue(expr3.test(Version.valueOf("1.2.3")));
    assertFalse(expr3.test(Version.valueOf("1.3.0")));
  }

  @Test
  public void shouldParseCaretRange() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> expr1 = parser.parse("^1");
    assertTrue(expr1.test(Version.valueOf("1.2.3")));
    assertFalse(expr1.test(Version.valueOf("3.2.1")));
    Predicate<Version> expr2 = parser.parse("^0.2");
    assertTrue(expr2.test(Version.valueOf("0.2.3")));
    assertFalse(expr2.test(Version.valueOf("0.3.0")));
    Predicate<Version> expr3 = parser.parse("^0.0.3");
    assertTrue(expr3.test(Version.valueOf("0.0.3")));
    assertFalse(expr3.test(Version.valueOf("0.0.4")));
  }

  @Test
  public void shouldParsePartialVersionRange() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> expr1 = parser.parse("1");
    assertTrue(expr1.test(Version.valueOf("1.2.3")));
    Predicate<Version> expr2 = parser.parse("2.0");
    assertTrue(expr2.test(Version.valueOf("2.0.9")));
  }

  @Test
  public void shouldParseWildcardRange() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Version v123 = Version.forIntegers(1, 2, 3);
    Version v321 = Version.forIntegers(3, 2, 1);
    Predicate<Version> expr1 = parser.parse("1.2.*");
    assertTrue(expr1.test(v123));
    assertFalse(expr1.test(v321));
    Predicate<Version> expr2 = parser.parse("1.x");
    assertTrue(expr2.test(v123));
    assertFalse(expr2.test(v321));
    Predicate<Version> expr3 = parser.parse("1.x.x");
    assertTrue(expr3.test(v123));
    assertFalse(expr3.test(v321));
    Predicate<Version> expr4 = parser.parse("X");
    assertTrue(expr4.test(v123));
    Predicate<Version> expr5 = parser.parse("X.X");
    assertTrue(expr5.test(v123));
    Predicate<Version> expr6 = parser.parse("X.X.X");
    assertTrue(expr6.test(v123));
  }

  @Test
  public void shouldParseHyphenRange() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> range = parser.parse("1.0.0 - 2.0.0");
    assertTrue(range.test(Version.valueOf("1.2.3")));
    assertFalse(range.test(Version.valueOf("3.2.1")));
  }

  @Test
  public void shouldNotGetConfusedByPreReleaseHyphens() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> range = parser.parse("1.0.0-pre2 - 2.0.0");
    assertTrue(range.test(Version.valueOf("1.2.3")));
    assertFalse(range.test(Version.valueOf("3.2.1")));
  }

  @Test
  public void shouldParseMultipleRangesJoinedWithAnd() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> and = parser.parse(">=1.0.0 & <2.0.0");
    assertTrue(and.test(Version.valueOf("1.2.3")));
    assertFalse(and.test(Version.valueOf("3.2.1")));
  }

  @Test
  public void shouldParseMultipleRangesJoinedWithOr() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> or = parser.parse("1.* | =2.0.0");
    assertTrue(or.test(Version.valueOf("1.2.3")));
    assertFalse(or.test(Version.valueOf("2.1.0")));
  }

  @Test
  public void shouldParseParenthesizedExpression() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> expr = parser.parse("(1)");
    assertTrue(expr.test(Version.valueOf("1.2.3")));
    assertFalse(expr.test(Version.valueOf("2.0.0")));
  }

  @Test
  public void shouldParseExpressionWithMultipleParentheses() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> expr = parser.parse("((1))");
    assertTrue(expr.test(Version.valueOf("1.2.3")));
    assertFalse(expr.test(Version.valueOf("2.0.0")));
  }

  @Test
  public void shouldParseNotExpression() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> not1 = parser.parse("!(1)");
    assertTrue(not1.test(Version.valueOf("2.0.0")));
    assertFalse(not1.test(Version.valueOf("1.2.3")));
    Predicate<Version> not2 = parser.parse("0.* & !(>=1 & <2)");
    assertTrue(not2.test(Version.valueOf("0.5.0")));
    assertFalse(not2.test(Version.valueOf("1.0.1")));
    Predicate<Version> not3 = parser.parse("!(>=1 & <2) & >=2");
    assertTrue(not3.test(Version.valueOf("2.0.0")));
    assertFalse(not3.test(Version.valueOf("1.2.3")));
  }

  @Test
  public void shouldRespectPrecedenceWhenUsedWithParentheses() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> expr1 = parser.parse("(~1.0 & <2.0) | >2.0");
    assertTrue(expr1.test(Version.valueOf("2.5.0")));
    Predicate<Version> expr2 = parser.parse("~1.0 & (<2.0 | >2.0)");
    assertFalse(expr2.test(Version.valueOf("2.5.0")));
  }

  @Test
  public void shouldParseComplexExpressions() {
    ExpressionParser parser = new ExpressionParser(new ExprLexer());
    Predicate<Version> expr = parser.parse(
        "((>=1.0.1+33 & <2+djjj3) | (>=3.0-rc.1 & <4)) & ((1-pre2+3.2-1.5) & (~1.5))");
    assertTrue(expr.test(Version.valueOf("1.5.0")));
    assertFalse(expr.test(Version.valueOf("2.5.0")));
  }
}
