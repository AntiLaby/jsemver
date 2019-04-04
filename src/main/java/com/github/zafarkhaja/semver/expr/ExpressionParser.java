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

import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.eq;
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.gt;
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.gte;
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.lt;
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.lte;
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.neq;
import static com.github.zafarkhaja.semver.expr.CompositeExpression.Helper.not;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.ALPHA_NUMERIC;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.AND;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.CARET;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.DOT;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.EOI;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.HYPHEN;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.LEFT_PAREN;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.NOT;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.NUMERIC;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.OR;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.PLUS;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.RIGHT_PAREN;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.TILDE;
import static com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken.Type.WILDCARD;

import com.github.zafarkhaja.semver.Parser;
import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.compiling.LexerException;
import com.github.zafarkhaja.semver.compiling.UnexpectedTokenException;
import com.github.zafarkhaja.semver.expr.ExprLexer.ExprToken;
import com.github.zafarkhaja.semver.util.Stream;
import com.github.zafarkhaja.semver.util.Stream.ElementType;
import com.github.zafarkhaja.semver.util.UnexpectedElementException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * A parser for the SemVer Expressions.
 *
 * @author Zafar Khaja &lt;zafarkhaja@gmail.com&gt;
 * @since 0.7.0
 */
public class ExpressionParser implements Parser<Predicate<Version>> {

  /**
   * The lexer instance used for tokenization of the input string.
   */
  private final ExprLexer lexer;

  /**
   * The stream of tokens produced by the lexer.
   */
  private Stream<ExprToken> tokens;

  /**
   * Constructs a {@code ExpressionParser} instance
   * with the corresponding lexer.
   *
   * @param lexer the lexer to use for tokenization of the input string
   */
  ExpressionParser(ExprLexer lexer) {
    this.lexer = lexer;
  }

  /**
   * Creates and returns new instance of the {@code ExpressionParser} class.
   *
   * <p>This method implements the Static Factory Method pattern.
   *
   * @return a new instance of the {@code ExpressionParser} class
   */
  public static Parser<Predicate<Version>> newInstance() {
    return new ExpressionParser(new ExprLexer());
  }

  /**
   * Parses the SemVer Expressions.
   *
   * @param input a string representing the SemVer Expression
   * @return the AST for the SemVer Expressions
   * @throws LexerException           when encounters an illegal character
   * @throws UnexpectedTokenException when consumes a token of an unexpectedToken type
   */
  @Override
  public Predicate<Version> parse(String input) {
    tokens = lexer.tokenize(input);
    Predicate<Version> expr = parseSemVerExpression();
    consumeNextToken(EOI);
    return expr;
  }

  /**
   * Parses the {@literal <semver-expr>} non-terminal.
   *
   * <pre>
   * {@literal
   * <semver-expr> ::= "(" <semver-expr> ")"
   *                 | "!" "(" <semver-expr> ")"
   *                 | <semver-expr> <more-expr>
   *                 | <range>
   * }
   * </pre>
   *
   * @return the expression AST
   */
  private CompositeExpression parseSemVerExpression() {
    CompositeExpression expr;
    if (tokens.positiveLookahead(NOT)) {
      tokens.consume();
      consumeNextToken(LEFT_PAREN);
      expr = not(parseSemVerExpression());
      consumeNextToken(RIGHT_PAREN);
    } else if (tokens.positiveLookahead(LEFT_PAREN)) {
      consumeNextToken(LEFT_PAREN);
      expr = parseSemVerExpression();
      consumeNextToken(RIGHT_PAREN);
    } else {
      expr = parseRange();
    }
    return parseMoreExpressions(expr);
  }

  /**
   * Parses the {@literal <more-expr>} non-terminal.
   *
   * <pre>
   * {@literal
   * <more-expr> ::= <boolean-op> <semver-expr> | epsilon
   * }
   * </pre>
   *
   * @param expr the left-hand expression of the logical operators
   * @return the expression AST
   */
  private CompositeExpression parseMoreExpressions(CompositeExpression expr) {
    if (tokens.positiveLookahead(AND)) {
      tokens.consume();
      expr = expr.and(parseSemVerExpression());
    } else if (tokens.positiveLookahead(OR)) {
      tokens.consume();
      expr = expr.or(parseSemVerExpression());
    }
    return expr;
  }

  /**
   * Parses the {@literal <range>} non-terminal.
   *
   * <pre>
   * {@literal
   * <expr> ::= <comparison-range>
   *          | <wildcard-expr>
   *          | <tilde-range>
   *          | <caret-range>
   *          | <hyphen-range>
   *          | <partial-version-range>
   * }
   * </pre>
   *
   * @return the expression AST
   */
  private CompositeExpression parseRange() {
    if (tokens.positiveLookahead(TILDE)) {
      return parseTildeRange();
    } else if (tokens.positiveLookahead(CARET)) {
      return parseCaretRange();
    } else if (isWildcardRange()) {
      return parseWildcardRange();
    } else if (isHyphenRange()) {
      return parseHyphenRange();
    } else if (isPartialVersionRange()) {
      return parsePartialVersionRange();
    }
    return parseComparisonRange();
  }

  /**
   * Parses the {@literal <comparison-range>} non-terminal.
   *
   * <pre>
   * {@literal
   * <comparison-range> ::= <comparison-op> <version> | <version>
   * }
   * </pre>
   *
   * @return the expression AST
   */
  private CompositeExpression parseComparisonRange() {
    ExprToken token = tokens.lookahead();
    CompositeExpression expr;
    switch ((ExprToken.Type) token.type) {
      case EQUAL:
        tokens.consume();
        expr = eq(parseVersion());
        break;
      case NOT_EQUAL:
        tokens.consume();
        expr = neq(parseVersion());
        break;
      case GREATER:
        tokens.consume();
        expr = gt(parseVersion());
        break;
      case GREATER_EQUAL:
        tokens.consume();
        expr = gte(parseVersion());
        break;
      case LESS:
        tokens.consume();
        expr = lt(parseVersion());
        break;
      case LESS_EQUAL:
        tokens.consume();
        expr = lte(parseVersion());
        break;
      default:
        expr = eq(parseVersion());
    }
    return expr;
  }

  /**
   * Parses the {@literal <tilde-range>} non-terminal.
   *
   * <pre>
   * {@literal
   * <tilde-range> ::= "~" <version>
   * }
   * </pre>
   *
   * @return the expression AST
   */
  private CompositeExpression parseTildeRange() {
    consumeNextToken(TILDE);
    int major = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    if (!tokens.positiveLookahead(DOT)) {
      return gte(versionFor(major)).and(lt(versionFor(major + 1)));
    }
    consumeNextToken(DOT);
    int minor = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    if (!tokens.positiveLookahead(DOT)) {
      return gte(versionFor(major, minor)).and(lt(versionFor(major, minor + 1)));
    }
    consumeNextToken(DOT);
    int patch = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    return gte(versionFor(major, minor, patch)).and(lt(versionFor(major, minor + 1)));
  }

  /**
   * Parses the {@literal <caret-range>} non-terminal.
   *
   * <pre>
   * {@literal
   * <caret-range> ::= "^" <version>
   * }
   * </pre>
   *
   * @return the expression AST
   */
  private CompositeExpression parseCaretRange() {
    consumeNextToken(CARET);
    int major = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    if (!tokens.positiveLookahead(DOT)) {
      return gte(versionFor(major)).and(lt(versionFor(major + 1)));
    }
    consumeNextToken(DOT);
    int minor = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    if (!tokens.positiveLookahead(DOT)) {
      Version lower = versionFor(major, minor);
      Version upper = major > 0 ? lower.incrementMajorVersion() : lower.incrementMinorVersion();
      return gte(lower).and(lt(upper));
    }
    consumeNextToken(DOT);
    int patch = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    Version version = versionFor(major, minor, patch);
    CompositeExpression gte = gte(version);
    if (major > 0) {
      return gte.and(lt(version.incrementMajorVersion()));
    } else if (minor > 0) {
      return gte.and(lt(version.incrementMinorVersion()));
    } else if (patch > 0) {
      return gte.and(lt(version.incrementPatchVersion()));
    }
    return eq(version);
  }

  /**
   * Determines if the following version terminals are part
   * of the {@literal <wildcard-range>} non-terminal.
   *
   * @return {@code true} if the following version terminals are
   *     part of the {@literal <wildcard-range>} non-terminal or
   *     {@code false} otherwise
   */
  private boolean isWildcardRange() {
    return isVersionFollowedBy(WILDCARD);
  }

  /**
   * Parses the {@literal <wildcard-range>} non-terminal.
   *
   * <pre>
   * {@literal
   * <wildcard-range> ::= <wildcard> (optional "." <wildcard> (optional "." <wildcard>))
   *                    | <major> "." <wildcard> (optional "." <wildcard>)
   *                    | <major> "." <minor> "." <wildcard>
   *
   * <wildcard> ::= "*" | "x" | "X"
   * }
   * </pre>
   *
   * @return the expression AST
   */
  private CompositeExpression parseWildcardRange() {
    if (tokens.positiveLookahead(WILDCARD)) {
      tokens.consume();
      //silently omit trailing .x in x.x or x.x.x
      if (tokens.positiveLookahead(DOT)) {
        tokens.consume();
        tokens.consume(WILDCARD);
      }
      if (tokens.positiveLookahead(DOT)) {
        tokens.consume();
        tokens.consume(WILDCARD);
      }
      return gte(versionFor(0, 0, 0));
    }

    int major = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    consumeNextToken(DOT);
    if (tokens.positiveLookahead(WILDCARD)) {
      tokens.consume();
      //silently omit trailing .x in eg. 2.x.x
      if (tokens.positiveLookahead(DOT)) {
        tokens.consume();
        tokens.consume(WILDCARD);
      }
      return gte(versionFor(major)).and(lt(versionFor(major + 1)));
    }

    int minor = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    consumeNextToken(DOT);
    consumeNextToken(WILDCARD);
    return gte(versionFor(major, minor)).and(lt(versionFor(major, minor + 1)));
  }

  /**
   * Determines if the following version terminals are
   * part of the {@literal <hyphen-range>} non-terminal.
   *
   * @return {@code true} if the following version terminals are
   *     part of the {@literal <hyphen-range>} non-terminal or
   *     {@code false} otherwise
   */
  private boolean isHyphenRange() {
    return isVersionFollowedBy(HYPHEN);
  }

  /**
   * Parses the {@literal <hyphen-range>} non-terminal.
   *
   * <pre>
   * {@literal
   * <hyphen-range> ::= <version> "-" <version>
   * }
   * </pre>
   *
   * @return the expression AST
   */
  private CompositeExpression parseHyphenRange() {
    CompositeExpression gte = gte(parseVersion());
    consumeNextToken(HYPHEN);
    return gte.and(lte(parseVersion()));
  }

  /**
   * Determines if the following version terminals are part
   * of the {@literal <partial-version-range>} non-terminal.
   *
   * @return {@code true} if the following version terminals are part
   *     of the {@literal <partial-version-range>} non-terminal or
   *     {@code false} otherwise
   */
  private boolean isPartialVersionRange() {
    if (!tokens.positiveLookahead(NUMERIC)) {
      return false;
    }
    EnumSet<ExprToken.Type> expected = EnumSet.complementOf(EnumSet.of(NUMERIC, DOT));
    return tokens.positiveLookaheadUntil(5, expected.toArray(new ExprToken.Type[expected.size()]));
  }

  /**
   * Parses the {@literal <partial-version-range>} non-terminal.
   *
   * <pre>
   * {@literal
   * <partial-version-range> ::= <major> | <major> "." <minor>
   * }
   * </pre>
   *
   * @return the expression AST
   */
  private CompositeExpression parsePartialVersionRange() {
    int major = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    if (!tokens.positiveLookahead(DOT)) {
      return gte(versionFor(major)).and(lt(versionFor(major + 1)));
    }
    consumeNextToken(DOT);
    int minor = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    return gte(versionFor(major, minor)).and(lt(versionFor(major, minor + 1)));
  }

  /**
   * Parses the {@literal <version>} non-terminal.
   *
   * <pre>
   * {@literal
   * <version> ::= <major>
   *             | <major> "." <minor>
   *             | <major> "." <minor> "." <patch>
   *             | <major> "." <minor> "." <patch> "-" <pre>
   *             | <major> "." <minor> "." <patch> "+" <build>
   *             | <major> "." <minor> "." <patch> "-" <pre> "+" <build>
   * }
   * </pre>
   *
   * @return the parsed version
   */
  private Version parseVersion() {
    final int major = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    int minor = 0;
    if (tokens.positiveLookahead(DOT)) {
      tokens.consume();
      minor = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    }
    int patch = 0;
    if (tokens.positiveLookahead(DOT)) {
      tokens.consume();
      patch = Integer.parseInt(consumeNextToken(NUMERIC).lexeme);
    }
    StringBuilder pre = new StringBuilder();
    if (tokens.positiveLookahead(HYPHEN) && tokens.lookahead(2).type == ALPHA_NUMERIC) {
      tokens.consume();
      while (tokens.positiveLookahead(ALPHA_NUMERIC, NUMERIC, DOT)) {
        pre.append(tokens.consume(ALPHA_NUMERIC, NUMERIC, DOT).lexeme);
      }
    }
    StringBuilder build = new StringBuilder();
    if (tokens.positiveLookahead(PLUS)) {
      tokens.consume();
      while (tokens.positiveLookahead(ALPHA_NUMERIC, NUMERIC, DOT)) {
        build.append(tokens.consume(ALPHA_NUMERIC, NUMERIC, DOT).lexeme);
      }
    }
    Version vtemp = Version.forIntegers(major, minor, patch);
    if (pre.length() != 0) {
      vtemp = vtemp.setPreReleaseVersion(pre.toString());
    }
    if (build.length() != 0) {
      vtemp = vtemp.setBuildMetadata(build.toString());
    }
    return vtemp;
  }

  /**
   * Determines if the version terminals are
   * followed by the specified token type.
   *
   * <p>This method is essentially a {@code lookahead(k)} method
   * which allows to solve the grammar's ambiguities.
   *
   * @param type the token type to check
   * @return {@code true} if the version terminals are followed by
   *     the specified token type or {@code false} otherwise
   */
  private boolean isVersionFollowedBy(ElementType<ExprToken> type) {
    Iterator<ExprToken> it = tokens.iterator();
    ExprToken lookahead = null;
    ExprToken lookahead2 = null;
    //skip first two nums and dots if possible else break
    for (int i = 0; i < 2; i++) {
      if (!it.hasNext() || (lookahead = it.next()).type != NUMERIC) {
        if (!it.hasNext() || (lookahead2 = it.next()).type != NUMERIC && lookahead2.type != HYPHEN
            && lookahead2.type != PLUS) {
          return type.isMatchedBy(lookahead);
        }
        break;
      }
      if (!it.hasNext() || (lookahead = it.next()).type != DOT) {
        return type.isMatchedBy(lookahead);
      }
    }
    lookahead = lookahead2 == null ? it.next() : lookahead2;
    //skip last number if need be
    if (lookahead.type == NUMERIC && it.hasNext()) {
      lookahead = it.next();
    }
    //filter out pre hyphens
    if (lookahead.type == HYPHEN && it.hasNext()) {
      // if alphanumeric, skip to the next token that cannot be used to denote a pre version
      ExprToken f = it.next();
      if (f.type == ALPHA_NUMERIC) {
        while (it.hasNext()) {
          lookahead = it.next();
          if (lookahead.type != ALPHA_NUMERIC && lookahead.type != NUMERIC
              && lookahead.type != DOT) {
            break;
          }
        }
      }
    }
    //skip builds
    if (lookahead.type == PLUS && it.hasNext()) {
      ExprToken f = it.next();
      // if alphanumeric or numeric,
      // skip to the next token that cannot be used to denote a build version
      if (f.type == ALPHA_NUMERIC || f.type == NUMERIC) {
        while (it.hasNext()) {
          lookahead = it.next();
          if (lookahead.type != ALPHA_NUMERIC && lookahead.type != NUMERIC
              && lookahead.type != DOT) {
            break;
          }
        }
      }
    }
    return type.isMatchedBy(lookahead);
  }

  /**
   * Creates a {@code Version} instance for the specified major version.
   *
   * @param major the major version number
   * @return the version for the specified major version
   */
  private Version versionFor(int major) {
    return versionFor(major, 0, 0);
  }

  /**
   * Creates a {@code Version} instance for
   * the specified major and minor versions.
   *
   * @param major the major version number
   * @param minor the minor version number
   * @return the version for the specified major and minor versions
   */
  private Version versionFor(int major, int minor) {
    return versionFor(major, minor, 0);
  }

  /**
   * Creates a {@code Version} instance for the
   * specified major, minor and patch versions.
   *
   * @param major the major version number
   * @param minor the minor version number
   * @param patch the patch version number
   * @return the version for the specified major, minor and patch versions
   */
  private Version versionFor(int major, int minor, int patch) {
    return Version.forIntegers(major, minor, patch);
  }

  /**
   * Tries to consume the next token in the stream.
   *
   * @param expected the expected types of the next token
   * @return the next token in the stream
   * @throws UnexpectedTokenException when encounters an unexpectedToken token type
   */
  private ExprToken consumeNextToken(ExprToken.Type... expected) {
    try {
      return tokens.consume(expected);
    } catch (UnexpectedElementException e) {
      throw new UnexpectedTokenException(e);
    }
  }
}
