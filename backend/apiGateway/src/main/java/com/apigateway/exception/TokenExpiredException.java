// TokenExpiredException.java
package com.apigateway.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String msg) {
        super(msg);
    }
}