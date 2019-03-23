package com.github.zafarkhaja.semver.maven;

import com.github.zafarkhaja.semver.Parser;
import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.util.Stream;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class RuleSet {

    private final Set<Predicate<Version>> rules;

    private RuleSet(Set<Predicate<Version>> rules) {
        this.rules = rules;
    }

    public static RuleSet fromInput(String input) {
        return null;
    }

    public boolean isSatisfiedBy(Version version) {
        for (Predicate<Version> rule : rules) if (!rule.test(version)) return false;
        return true;
    }

    public static class Builder implements Parser<RuleSet> {

        @Override
        public RuleSet parse(String input) {
            Stream<Lexer.Token> stream = new Lexer().tokenize(input);
            Set<Predicate<Version>> ruleset = new HashSet<>();
            switch (stream.lookahead().type) {
                case LEFT_SQBR:
                    ruleset.add(startRule(stream));
                    break;
                case LEFT_PAREN:
                    ruleset.add(startRule(stream));
                    break;
                default:
                    return null;
            }
            return new RuleSet(Collections.unmodifiableSet(ruleset));
        }

        private Predicate<Version> startRule(Stream<Lexer.Token> stream) {
            return null;
        }
    }
}
