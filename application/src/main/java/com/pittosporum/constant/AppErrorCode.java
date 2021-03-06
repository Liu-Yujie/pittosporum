package com.pittosporum.constant;

import pittosporum.constant.ErrorCode;

/**
 * @author yichen(graffitidef @ gmail.com)
 */

public enum AppErrorCode implements ErrorCode {
    PARAMS_IS_EMPTY("B0001", "params is empty when call api"),
    DUPLICATE_RECORD("B0002", "This record already exists in the system."),
    EMPTY_OBJECT("B0003", "empty object"),
    EXECUTE_SQL_ERROR("B0004", "an error occurred while running the sql"),
    ;

    private final String statusCode;
    private final String message;

    AppErrorCode(String statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }


    public String getStatusCode() {
        return this.statusCode;
    }

    public String getMessage() {
        return this.message;
    }
}
