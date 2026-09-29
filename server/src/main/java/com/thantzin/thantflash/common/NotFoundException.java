package com.thantzin.thantflash.common;

/** Thrown when a resource does not exist or belongs to another user (both → 404). */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String what, Long id) {
        super(what + " " + id + " not found");
    }
}
