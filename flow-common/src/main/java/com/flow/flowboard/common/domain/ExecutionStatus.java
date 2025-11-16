package com.flow.flowboard.common.domain;

/**
 * Represents the status of a workflow execution.
 */
public enum ExecutionStatus {
    /**
     * Execution has been created but not yet started.
     */
    PENDING,

    /**
     * Execution is currently running.
     */
    RUNNING,

    /**
     * Execution completed successfully.
     */
    SUCCESS,

    /**
     * Execution failed due to an error.
     */
    FAILED,

    /**
     * Execution was cancelled by user or system.
     */
    CANCELLED
}
