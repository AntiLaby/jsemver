package com.github.zafarkhaja.semver.expr;

import com.github.zafarkhaja.semver.Version;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Maven Parser tests.
 *
 * @author heisluft
 */
public class MavenParserTest {
  @Test
  public void souldAcceptAllMavenRangesCorrectly() {
    Version v = Version.forIntegers(1, 8, 23);
    Predicate<Version> set = new MavenParser().parse("[1.4-pre+2,1.8.23+00011]");
    Assertions.assertTrue(set.test(v));
    Predicate<Version> set2 = new MavenParser().parse("[,1.8.23+00011]");
    Assertions.assertTrue(set2.test(v));
    Predicate<Version> set3 = new MavenParser().parse("(,1.8.23+00011]");
    Assertions.assertTrue(set3.test(v));
    Predicate<Version> set4 = new MavenParser().parse("[1.8.23+00011]");
    Assertions.assertTrue(set4.test(v));
    Predicate<Version> set5 = new MavenParser().parse("(,1.8.23),(1.8.24,)");
    Assertions.assertFalse(set5.test(v));
    Assertions.assertFalse(set5.test(v.incrementPatchVersion()));
    Assertions.assertTrue(set5.test(v.withPatch(25, true)));
  }
}
