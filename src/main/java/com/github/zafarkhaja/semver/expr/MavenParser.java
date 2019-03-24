package com.github.zafarkhaja.semver.expr;

import com.github.zafarkhaja.semver.Parser;
import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.compiling.UnexpectedTokenException;
import com.github.zafarkhaja.semver.util.Stream;

import java.util.function.Predicate;

import static com.github.zafarkhaja.semver.expr.MvnLexer.MvnToken.Type.*;

public class RuleSet implements Parser<Predicate<Version>> {

    @Override
    public Predicate<Version> parse(String input) {
        Stream<MvnLexer.MvnToken> stream = new MvnLexer().tokenize(input);
        Predicate<Version> rule = version -> false;
        while (!stream.positiveLookahead(EOI)) {
            rule = rule.or(nextRule(stream));
            if (stream.consume(COMMA, EOI).type == EOI) break;
            else if (stream.lookahead().type == EOI)
                throw new UnexpectedTokenException(stream.lookahead(), LEFT_PAREN, LEFT_SQBR);
        }
        return rule;
    }

    private Predicate<Version> nextRule(Stream<MvnLexer.MvnToken> stream) {
        MvnLexer.MvnToken l = stream.consume(LEFT_SQBR, LEFT_PAREN);
        Version v1 = stream.positiveLookahead(NUMERIC) ? parseVersion(stream) : null;
        Predicate<Version> part1 = v1 == null ? (v) -> true : l.lexeme.equals("[") ? new GreaterOrEqual(v1) : new Greater(v1);
        //[1.1] evaluates to EQUALS 1.1 w/o buildnums
        if (l.type == LEFT_SQBR && v1 != null && stream.positiveLookahead(RIGHT_SQBR)) {
            stream.consume();
            return version -> v1.compareTo(version) == 0;
        }
        stream.consume(COMMA);
        Version v2 = stream.positiveLookahead(NUMERIC) ? parseVersion(stream) : null;
        MvnLexer.MvnToken r = stream.consume(RIGHT_SQBR, RIGHT_PAREN);
        Predicate<Version> part2 = v2 == null ? (v) -> true : r.lexeme.equals("]") ? new LessOrEqual(v2) : new Less(v2);
        return part1.and(part2);
    }

    /**
     * Parses the {@literal <version>} non-terminal
     *
     * @return the parsed version
     */
    private Version parseVersion(Stream<MvnLexer.MvnToken> tokens) {
        int major = Integer.parseInt(tokens.consume(NUMERIC).lexeme);
        int minor = 0;
        if (tokens.positiveLookahead(DOT)) {
            tokens.consume();
            minor = Integer.parseInt(tokens.consume(NUMERIC).lexeme);
        }
        int patch = 0;
        if (tokens.positiveLookahead(DOT)) {
            tokens.consume();
            patch = Integer.parseInt(tokens.consume(NUMERIC).lexeme);
        }
        String pre = null;
        if (tokens.positiveLookahead(HYPHEN)) {
            tokens.consume();
            pre = tokens.consume(ALPHA_NUMERIC, NUMERIC).lexeme;
        }
        String build = null;
        if (tokens.positiveLookahead(PLUS)) {
            tokens.consume();
            build = tokens.consume(ALPHA_NUMERIC, NUMERIC).lexeme;
        }
        Version vtemp = Version.forIntegers(major, minor, patch);
        if (pre != null) vtemp = vtemp.setPreReleaseVersion(pre);
        if (build != null) vtemp = vtemp.setBuildMetadata(build);
        return vtemp;
    }
}
