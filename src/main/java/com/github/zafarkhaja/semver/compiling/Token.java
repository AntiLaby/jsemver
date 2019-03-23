package com.github.zafarkhaja.semver.compiling;

import com.github.zafarkhaja.semver.util.Stream;

public abstract class Token<E extends Token> {

    public interface Type<E extends Token> extends Stream.ElementType<E> {
        String name();
    }

    /**
     * The type of this token.
     */
    public final Type<E> type;

    /**
     * The lexeme of this token.
     */
    public final String lexeme;

    /**
     * The position of this token.
     */
    public final int position;

    /**
     * Constructs a {@code Token} instance
     * with the type, lexeme and position.
     *
     * @param type the type of this token
     * @param lexeme the lexeme of this token
     * @param position the position of this token
     */
    protected Token(Type<E> type, String lexeme, int position) {
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
        if (!(other instanceof Token)) {
            return false;
        }
        Token token = (Token) other;
        return
                type.equals(token.type) &&
                        lexeme.equals(token.lexeme) &&
                        position == token.position;
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
        return String.format(
                "%s(%s) at position %d",
                type.name(),
                lexeme, position
        );
    }
}
