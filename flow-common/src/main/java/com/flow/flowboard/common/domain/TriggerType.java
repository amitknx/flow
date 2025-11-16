package com.flow.flowboard.common.domain;

/**
 * Represents how a workflow execution was triggered.
 */
public enum TriggerType {
    /**
     * Manually triggered by a user through the UI.
     */
    MANUAL,

    /**
     * Triggered programmatically through the API.
     */
    API,

    /**
     * Triggered by a scheduled job or cron expression.
     */
    SCHEDULED
}
