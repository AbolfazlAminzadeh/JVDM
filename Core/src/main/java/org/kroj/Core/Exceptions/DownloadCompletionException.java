package org.kroj.Core.Exceptions;

public class DownloadCompletionException extends RuntimeException {
    public DownloadCompletionException(Exception e) {
        super(e);
    }
}
