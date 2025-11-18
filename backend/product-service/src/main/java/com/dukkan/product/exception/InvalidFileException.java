package com.dukkan.product.exception;

/**
 * Exception thrown when file upload validation fails.
 * Used for invalid file types, sizes, or formats.
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
