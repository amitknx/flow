package com.flow.flowboard.common.domain;

/**
 * Represents the status of a step execution within a workflow.
 */
public enum StepStatus {
    /**
     * Step has been created but not yet started.
     */
    PENDING,

    /**
     * Step is currently executing.
     */
    RUNNING,

    /**
     * Step completed successfully.
     */
    SUCCESS,

    /**
     * Step failed due to an error.
     */
    FAILED,

    /**
     * Step was skipped (e.g., conditional branch not taken).
     */
    SKIPPED
}
