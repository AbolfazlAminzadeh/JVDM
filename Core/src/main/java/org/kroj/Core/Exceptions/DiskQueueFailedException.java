package org.kroj.Core.Exceptions;

public class DiskQueueFailedException extends RuntimeException{
    public DiskQueueFailedException(String message) {
        super(message);
    }
}
