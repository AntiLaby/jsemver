package com.github.zafarkhaja.semver.expr;

import com.github.zafarkhaja.semver.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MvnTest {
    @Test
    public void doStuff() {
        Version v = Version.forIntegers(1,8,2);
        RuleSet set = new RuleSet.Builder().parse("[1.4-pre+2,1.8.23+00011)");
        Assertions.assertTrue(set.isSatisfiedBy(v));
        RuleSet set2 = new RuleSet.Builder().parse("[,1.8.23+00011)");
        Assertions.assertTrue(set2.isSatisfiedBy(v));
        RuleSet set3 = new RuleSet.Builder().parse("(,1.8.23+00011)");
        Assertions.assertTrue(set3.isSatisfiedBy(v));
        RuleSet set4 = new RuleSet.Builder().parse("[1.8.23+00011]");
        Assertions.assertTrue(set4.isSatisfiedBy(v));
    }
}
