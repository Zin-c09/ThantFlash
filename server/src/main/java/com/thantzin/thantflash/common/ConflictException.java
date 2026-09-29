package com.thantzin.thantflash.common;

/** Thrown when a unique value (username, deck name) is already taken → 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
