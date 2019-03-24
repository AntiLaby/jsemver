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

import com.github.zafarkhaja.semver.Parser;
import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.compiling.UnexpectedTokenException;
import com.github.zafarkhaja.semver.util.Stream;

import java.util.function.Predicate;

import static com.github.zafarkhaja.semver.expr.MvnLexer.MvnToken.Type.*;

/**
 * A Parser for Maven Version range strings. NOT Thread-safe.
 *
 * @author heisluft &lt;heisluftlp@gmail.com&gt;
 * @since 0.10.0
 */
public class MavenParser implements Parser<Predicate<Version>> {

    /**
     * {@inheritDoc}
     */
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

    /**
     * Parses the next rule non-terminal.
     *
     * @param stream the stream to operate on
     * @return the next rule
     * @throws UnexpectedTokenException if an unexpected token is encountered
     */
    private Predicate<Version> nextRule(Stream<MvnLexer.MvnToken> stream) throws UnexpectedTokenException {
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
     * @param stream the stream to operate on
     * @return the parsed version
     * @throws UnexpectedTokenException if an unexpected token is encountered
     */
    private Version parseVersion(Stream<MvnLexer.MvnToken> stream) throws UnexpectedTokenException {
        int major = Integer.parseInt(stream.consume(NUMERIC).lexeme);
        int minor = 0;
        if (stream.positiveLookahead(DOT)) {
            stream.consume();
            minor = Integer.parseInt(stream.consume(NUMERIC).lexeme);
        }
        int patch = 0;
        if (stream.positiveLookahead(DOT)) {
            stream.consume();
            patch = Integer.parseInt(stream.consume(NUMERIC).lexeme);
        }
        StringBuilder pre = new StringBuilder();
        if (stream.positiveLookahead(HYPHEN)) {
            stream.consume();
            while (stream.positiveLookahead(ALPHA_NUMERIC, NUMERIC, DOT)) {
                pre.append(stream.consume(ALPHA_NUMERIC, NUMERIC, DOT).lexeme);
            }}
        StringBuilder build = new StringBuilder();
        if (stream.positiveLookahead(PLUS)) {
            stream.consume();
            while (stream.positiveLookahead(ALPHA_NUMERIC, NUMERIC, DOT)) {
                build.append(stream.consume(ALPHA_NUMERIC, NUMERIC, DOT).lexeme);
            }
        }
        Version vtemp = Version.forIntegers(major, minor, patch);
        if (pre.length() != 0) vtemp = vtemp.setPreReleaseVersion(pre.toString());
        if (build.length() != 0) vtemp = vtemp.setBuildMetadata(build.toString());
        return vtemp;
    }
}
