package org.csea.job;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Contains the result of a Job.
 */
public class JobResult {

    private final UUID jobId;

    private JobExecutionStatus status;

    private String failedMessage;

    public JobResult(@Nonnull UUID jobId) {
        this(jobId, JobExecutionStatus.PENDING);
    }

    public JobResult(@Nonnull UUID jobId, @Nonnull JobExecutionStatus status) {
        this(jobId, status, null);
    }

    public JobResult(@Nonnull UUID jobId, @Nonnull JobExecutionStatus status, @Nullable String failedMessage) {
        this.jobId = jobId;
        this.status = status;
        this.failedMessage  = failedMessage;
    }

    /**
     * The identifier of the <code>Job</code> for which this is the result
     * @return a <code>UUID</code>
     */
    @Nonnull
    public UUID getJobId() {
        return jobId;
    }

    @Nonnull
    public JobExecutionStatus getStatus() {
        return status;
    }

    /**
     * If the <code>Job</code> was not successful, a message that describes what the problem was
     *
     * @return a <code>String</code>.  Can be null
     */
    @Nullable
    public String getFailedMessage() {
        return failedMessage;
    }

//    public void running() {
//        setStatusAndNotify(JobExecutionStatus.RUNNING);
//    }
//
//    public void success() {
//        setStatusAndNotify(JobExecutionStatus.SUCCESS);
//    }
//
//    public void fail(String failedMessage) {
//        this.failedMessage = failedMessage;
//        setStatusAndNotify(JobExecutionStatus.FAIL);
//    }

    public void await(long timeoutMillis) throws InterruptedException {
        synchronized (this) {
            this.wait(timeoutMillis);
        }
    }
    public void copy(JobResult other) {
        this.status = other.status;
        this.failedMessage = other.failedMessage;
        notify();
    }

}
