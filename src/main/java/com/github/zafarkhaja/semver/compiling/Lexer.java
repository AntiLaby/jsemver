package com.github.zafarkhaja.semver.compiling;

import com.github.zafarkhaja.semver.util.Stream;
import com.github.zafarkhaja.semver.util.UnexpectedElementException;

public abstract class Lexer<T extends Token> {
    protected abstract Stream<T> tokenize(String input) throws UnexpectedElementException;
}
