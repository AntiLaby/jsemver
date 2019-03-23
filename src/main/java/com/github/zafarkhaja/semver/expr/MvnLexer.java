package com.github.zafarkhaja.semver.expr;

import com.github.zafarkhaja.semver.compiling.Lexer;
import com.github.zafarkhaja.semver.compiling.LexerException;
import com.github.zafarkhaja.semver.compiling.Token;
import com.github.zafarkhaja.semver.util.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MvnLexer extends Lexer<MvnLexer.MvnToken> {
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
