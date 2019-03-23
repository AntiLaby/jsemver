package com.github.zafarkhaja.semver.maven;

import com.github.zafarkhaja.semver.compiling.LexerException;
import com.github.zafarkhaja.semver.util.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    Stream<Lexer.Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int tokenPos = 0;
        while (!input.isEmpty()) {
            boolean matched = false;
            for (Token.Type tokenType : Token.Type.values()) {
                Matcher matcher = tokenType.pattern.matcher(input);
                if (matcher.find()) {
                    matched = true;
                    input = matcher.replaceFirst("");
                    tokens.add(new Token(tokenType, matcher.group(), tokenPos));
                    tokenPos += matcher.end();
                    break;
                }
            }
            if (!matched) {
                throw new LexerException(input);
            }
        }
        tokens.add(new Token(Token.Type.EOI, null, tokenPos));
        return new Stream<>(tokens.toArray(new Token[tokens.size()]));
    }

    static class Token {
        /**
         * The type of this token.
         */
        final Lexer.Token.Type type;
        /**
         * The lexeme of this token.
         */
        final String lexeme;
        /**
         * The position of this token.
         */
        final int position;

        /**
         * Constructs a {@code Token} instance with the type, lexeme and position.
         *
         * @param type     the type of this token
         * @param lexeme   the lexeme of this token
         * @param position the position of this token
         */
        Token(Lexer.Token.Type type, String lexeme, int position) {
            this.type = type;
            this.lexeme = (lexeme == null) ? "" : lexeme;
            this.position = position;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Lexer.Token)) {
                return false;
            }
            Lexer.Token token = (Lexer.Token) other;
            return type.equals(token.type) && lexeme.equals(token.lexeme) && position == token.position;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + type.hashCode();
            hash = 71 * hash + lexeme.hashCode();
            hash = 71 * hash + position;
            return hash;
        }

        /**
         * Returns the string representation of this token.
         *
         * @return the string representation of this token
         */
        @Override
        public String toString() {
            return String.format("%s(%s) at position %d", type.name(), lexeme, position);
        }

        enum Type implements Stream.ElementType<Token> {
            NUMERIC("0|[1-9][0-9]*"),
            DOT("\\."),
            HYPHEN("-"),
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
            public boolean isMatchedBy(Token token) {
                return token != null && this == token.type;
            }
        }
    }
}
