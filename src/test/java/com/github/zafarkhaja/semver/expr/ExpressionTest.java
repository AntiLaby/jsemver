package com.github.zafarkhaja.semver.expr;

import com.github.zafarkhaja.semver.Version;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExpressionTest {
  @Nested
  public class LessTest {

    @Test
    public void shouldCheckIfVersionIsLessThanParsedVersion() {
      Version parsed = Version.valueOf("2.0.0");
      Less lt = new Less(parsed);
      assertTrue(lt.interpret(Version.valueOf("1.2.3")));
      assertFalse(lt.interpret(Version.valueOf("3.2.1")));
    }
  }

  @Nested
  public class LessOrEqualTest {

    @Test
    public void shouldCheckIfVersionIsLessThanOrEqualToParsedVersion() {
      Version parsed = Version.valueOf("2.0.0");
      LessOrEqual le = new LessOrEqual(parsed);
      assertTrue(le.interpret(Version.valueOf("1.2.3")));
      assertTrue(le.interpret(Version.valueOf("2.0.0")));
      assertFalse(le.interpret(Version.valueOf("3.2.1")));
    }
  }

  @Nested
  public class NotTest {

    @Test
    public void shouldRevertBooleanResultOfExpression() {
      Expression expr1 = version -> false;
      Expression expr2 = version -> true;
      Not not;
      not = new Not(expr1);
      assertTrue(not.interpret(null));
      not = new Not(expr2);
      assertFalse(not.interpret(null));
    }
  }

  @Nested
  public class OrTest {

    @Test
    public void shouldCheckIfOneOfTwoExpressionsEvaluateToTrue() {
      Expression left = version -> false;
      Expression right = version -> true;
      Or or = new Or(left, right);
      assertTrue(or.interpret(null));
    }
  }

  @Nested
  public class NotEqualTest {

    @Test
    public void shouldCheckIfVersionIsNotEqualToParsedVersion() {
      Version parsed = Version.valueOf("1.2.3");
      NotEqual ne = new NotEqual(parsed);
      assertTrue(ne.interpret(Version.valueOf("3.2.1")));
      assertFalse(ne.interpret(Version.valueOf("1.2.3")));
    }
  }

  @Nested
  public class GreaterTest {

    @Test
    public void shouldCheckIfVersionIsGreaterThanParsedVersion() {
      Version parsed = Version.valueOf("2.0.0");
      Greater gt = new Greater(parsed);
      assertTrue(gt.interpret(Version.valueOf("3.2.1")));
      assertFalse(gt.interpret(Version.valueOf("1.2.3")));
    }
  }

  @Nested
  public class AndTest {

    @Test
    public void shouldCheckIfBothExpressionsEvaluateToTrue() {
      Expression left = version -> true;
      Expression right = version -> true;
      And and = new And(left, right);
      assertTrue(and.interpret(null));
    }
  }

  @Nested
  public class EqualTest {

    @Test
    public void shouldCheckIfVersionIsEqualToParsedVersion() {
      Version parsed = Version.valueOf("1.2.3");
      Equal eq = new Equal(parsed);
      assertTrue(eq.interpret(Version.valueOf("1.2.3")));
      assertFalse(eq.interpret(Version.valueOf("3.2.1")));
    }
  }

  @Nested
  public class GreaterOrEqualTest {

    @Test
    public void shouldCheckIfVersionIsGreaterThanOrEqualToParsedVersion() {
      Version parsed = Version.valueOf("2.0.0");
      GreaterOrEqual ge = new GreaterOrEqual(parsed);
      assertTrue(ge.interpret(Version.valueOf("3.2.1")));
      assertTrue(ge.interpret(Version.valueOf("2.0.0")));
      assertFalse(ge.interpret(Version.valueOf("1.2.3")));
    }
  }

  @Nested
  public class CompositeExpressionTest {

    @Test
    public void shouldSupportEqualExpression() {
      assertTrue(eq("1.0.0").interpret("1.0.0"));
      assertFalse(eq("1.0.0").interpret("2.0.0"));
    }

    @Test
    public void shouldSupportNotEqualExpression() {
      assertTrue(neq("1.0.0").interpret("2.0.0"));
    }

    @Test
    public void shouldSupportGreaterExpression() {
      assertTrue(gt("1.0.0").interpret("2.0.0"));
      assertFalse(gt("2.0.0").interpret("1.0.0"));
    }

    @Test
    public void shouldSupportGreaterOrEqualExpression() {
      assertTrue(gte("1.0.0").interpret("1.0.0"));
      assertTrue(gte("1.0.0").interpret("2.0.0"));
      assertFalse(gte("2.0.0").interpret("1.0.0"));
    }

    @Test
    public void shouldSupportLessExpression() {
      assertTrue(lt("2.0.0").interpret("1.0.0"));
      assertFalse(lt("1.0.0").interpret("2.0.0"));
    }

    @Test
    public void shouldSupportLessOrEqualExpression() {
      assertTrue(lte("1.0.0").interpret("1.0.0"));
      assertTrue(lte("2.0.0").interpret("1.0.0"));
      assertFalse(lte("1.0.0").interpret("2.0.0"));
    }

    @Test
    public void shouldSupportNotExpression() {
      assertTrue(not(eq("1.0.0")).interpret("2.0.0"));
      assertFalse(not(eq("1.0.0")).interpret("1.0.0"));
    }

    @Test
    public void shouldSupportAndExpression() {
      assertTrue(gt("1.0.0").and(lt("2.0.0")).interpret("1.5.0"));
      assertFalse(gt("1.0.0").and(lt("2.0.0")).interpret("2.5.0"));
    }

    @Test
    public void shouldSupportOrExpression() {
      assertTrue(lt("1.0.0").or(gt("1.0.0")).interpret("1.5.0"));
      assertFalse(gt("1.0.0").or(gt("2.0.0")).interpret("0.5.0"));
    }

    @Test
    public void shouldSupportComplexExpressions() {
      /* ((>=1.0.1 & <2) | (>=3.0 & <4)) & ((1-1.5) & (~1.5)) */
      CompositeExpression expr = gte("1.0.1").and(lt("2.0.0").or(gte("3.0.0").and(
          lt("4.0.0").and(gte("1.0.0").and(lte("1.5.0").and(gte("1.5.0").and(lt("2.0.0"))))))));
      assertTrue(expr.interpret("1.5.0"));
      assertFalse(expr.interpret("2.5.0"));
    }
  }
}
