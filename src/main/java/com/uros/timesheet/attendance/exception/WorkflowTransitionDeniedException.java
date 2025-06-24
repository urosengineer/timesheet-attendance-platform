package com.uros.timesheet.attendance.exception;

public class WorkflowTransitionDeniedException extends RuntimeException {
    public WorkflowTransitionDeniedException(String message) {
        super(message);
    }
}