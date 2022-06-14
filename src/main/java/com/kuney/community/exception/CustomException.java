package com.kuney.community.exception;

/**
 * @author kuneychen
 * @since 2022/6/14 14:05
 */
public class CustomException extends RuntimeException {

    private int code;

    public CustomException() {
    }

    public CustomException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
