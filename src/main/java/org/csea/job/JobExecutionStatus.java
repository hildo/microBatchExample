package org.csea.job;

import java.util.EnumSet;

public enum JobExecutionStatus {
    PENDING(false),
    RUNNING(false),
    SUCCESS(true),
    FAIL(true);

    private final boolean complete;

    JobExecutionStatus(boolean complete) {
        this.complete = complete;
    }

    boolean isComplete() {
        return complete;
    }
}
