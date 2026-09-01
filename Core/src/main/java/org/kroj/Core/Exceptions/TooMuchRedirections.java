package org.kroj.Core.Exceptions;

public class TooMuchRedirections extends RuntimeException {
    public TooMuchRedirections(String message) {
        super(message);
    }
}
