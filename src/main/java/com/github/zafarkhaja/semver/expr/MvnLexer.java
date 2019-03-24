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

import com.github.zafarkhaja.semver.compiling.Lexer;
import com.github.zafarkhaja.semver.compiling.LexerException;
import com.github.zafarkhaja.semver.compiling.Token;
import com.github.zafarkhaja.semver.util.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Lexer for maven version ranges.
 *
 * @author heisluft &lt;heisluftlp@gmail.com&gt;
 * @since 0.10.0
 */
class MvnLexer extends Lexer<MvnLexer.MvnToken> {
    /**
     * {@inheritDoc}
     */
    protected Stream<MvnToken> tokenize(String input) {
        List<MvnToken> tokens = new ArrayList<>();
        int tokenPos = 0;
        while (!input.isEmpty()) {
            boolean matched = false;
            for (MvnToken.Type tokenType : MvnToken.Type.values()) {
                Matcher matcher = tokenType.pattern.matcher(input);
                if (matcher.find()) {
                    matched = true;
                    input = matcher.replaceFirst("");
                    tokens.add(new MvnToken(tokenType, matcher.group(), tokenPos));
                    tokenPos += matcher.end();
                    break;
                }
            }
            if (!matched) {
                throw new LexerException(input);
            }
        }
        tokens.add(new MvnToken(MvnToken.Type.EOI, null, tokenPos));
        return new Stream<>(tokens.toArray(new MvnToken[tokens.size()]));
    }

    /**
     * Represents a token in maven version range expression language
     */
    static class MvnToken extends Token<MvnToken> {
        /**
         * Constructs a {@code Token} instance with the type, lexeme and position.
         *
         * @param type     the type of this token
         * @param lexeme   the lexeme of this token
         * @param position the position of this token
         */
        MvnToken(MvnToken.Type type, String lexeme, int position) {
            super(type, (lexeme == null) ? "" : lexeme, position);
        }

        enum Type implements Token.Type<MvnToken> {
            ALPHA_NUMERIC("(\\d*\\p{L}(\\p{L}|\\d)*)|0\\d+"),
            NUMERIC("0|[1-9][0-9]*"),
            DOT("\\."),
            HYPHEN("-"),
            PLUS("\\+"),
            LEFT_SQBR("\\["),
            RIGHT_SQBR("\\]"),
            LEFT_PAREN("\\("),
            RIGHT_PAREN("\\)"),
            COMMA(","),
            EOI("?!");

            /**
             * A pattern matching this type.
             */
            final Pattern pattern;

            /**
             * Constructs a token type with a regular expression for the pattern.
             *
             * @param regexp the regular expression for the pattern
             * @see #pattern
             */
            Type(String regexp) {
                pattern = Pattern.compile("^(" + regexp + ")");
            }

            /**
             * Returns the string representation of this type.
             *
             * @return the string representation of this type
             */
            @Override
            public String toString() {
                return name() + "(" + pattern + ")";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isMatchedBy(MvnToken token) {
                return token != null && this == token.type;
            }
        }
    }
}
