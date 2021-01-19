package com.springboot.employees.exceptions;

import java.util.Date;

public class ErrorDetails {

    private Date timestamp;
    private String message;
    private String errorId;
    private int httpStatus;
    private String errorType;


    public ErrorDetails(Date timestamp, String errorId, String message) {
        super();
        this.timestamp = timestamp;
        this.message = message;
        this.errorId = errorId;

    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
}
