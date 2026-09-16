package org.Kroj.Core.Tools.Exceptions;

public class FailToBindSocketException extends RuntimeException {
    public FailToBindSocketException(String message) {
        super(message);
    }
    public FailToBindSocketException(Throwable throwable) {
        super(throwable);
    }
}
