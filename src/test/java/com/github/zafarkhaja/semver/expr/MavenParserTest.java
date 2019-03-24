package com.github.zafarkhaja.semver.expr;

import com.github.zafarkhaja.semver.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

public class MvnTest {
    @Test
    public void doStuff() {
        Version v = Version.forIntegers(1,8,23);
        Predicate<Version> set = new RuleSet().parse("[1.4-pre+2,1.8.23+00011]");
        Assertions.assertTrue(set.test(v));
        Predicate<Version> set2 = new RuleSet().parse("[,1.8.23+00011]");
        Assertions.assertTrue(set2.test(v));
        Predicate<Version> set3 = new RuleSet().parse("(,1.8.23+00011]");
        Assertions.assertTrue(set3.test(v));
        Predicate<Version> set4 = new RuleSet().parse("[1.8.23+00011]");
        Assertions.assertTrue(set4.test(v));
        Predicate<Version> set5 = new RuleSet().parse("(,1.8.23),(1.8.24,)");
        Assertions.assertFalse(set5.test(v));
        Assertions.assertFalse(set5.test(v.incrementPatchVersion()));
        Assertions.assertTrue(set5.test(v.withPatch(25, true)));
    }
}
