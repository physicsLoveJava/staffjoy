package org.spearhead.faraday.faraday.exceptions;

public class FaradayException extends RuntimeException {
    public FaradayException(String message) {
        super(message);
    }

    public FaradayException(String message, Throwable cause) {
        super(message, cause);
    }
}
