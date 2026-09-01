package org.kroj.Core.Exceptions;

public class FailToBindSocketException extends RuntimeException {

    public FailToBindSocketException(String message, Throwable cause) {
        super(message, cause);
    }
    public FailToBindSocketException(Throwable cause) {
        super(cause);
    }

    public FailToBindSocketException(String message) {
        super(message);
    }

}
