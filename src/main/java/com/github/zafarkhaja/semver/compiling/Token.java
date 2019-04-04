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

package com.github.zafarkhaja.semver.compiling;

import com.github.zafarkhaja.semver.util.Stream;

/**
 * A general Token.
 * A token consists of its type, a lexeme (the characters that matched the type)
 * and the position where it was matched.
 *
 * @param <E> the type of token. Used for binding the Token.Type
 * @author heisluft &lt;heisluftlp@gmail.com&gt;
 * @since 0.10.0
 */
public abstract class Token<E extends Token> {

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
  private final int position;

  /**
   * Constructs a {@code Token} instance
   * with the type, lexeme and position.
   *
   * @param type     the type of this token
   * @param lexeme   the lexeme of this token
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
        type.equals(token.type)
            && lexeme.equals(token.lexeme)
            && position == token.position;
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

  /**
   * A token type interface is a pattern that characters have to match to form a token.
   *
   * @param <E> the token the type is bound to
   */
  public interface Type<E extends Token> extends Stream.ElementType<E> {
    String name();
  }
}
