package com.uros.timesheet.attendance.exception;

public class ExportException extends RuntimeException {
    public ExportException(String localizedMessage) {
        super(localizedMessage);
    }
}