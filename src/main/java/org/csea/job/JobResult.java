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

    /**
     * Creates a new JobResult instance.
     *
     * The status attribute will be PENDING.
     * The failedMessage attribute will be null.
     *
     * @param jobId the <code>UUID</code> that defines the Job Id for this result
     */
    public JobResult(@Nonnull UUID jobId) {
        this(jobId, JobExecutionStatus.PENDING);
    }

    /**
     * Creates a new JobResult instance.
     *
     * The failedMessage attribute will be null.
     *
     * @param jobId the <code>UUID</code> that defines the Job Id for this result
     * @param status the <code>JobExecutionStatus</code> for this result
     */
    public JobResult(@Nonnull UUID jobId, @Nonnull JobExecutionStatus status) {
        this(jobId, status, null);
    }

    /**
     * Creates a new JobResult instance
     *
     * @param jobId the <code>UUID</code> that defines the Job Id for this result
     * @param status the <code>JobExecutionStatus</code> for this result
     * @param failedMessage a <code>String</code> which contains a failed message.  Can be null.
     */
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

    /**
     * Gets the <code>JobExecutionStatus</code> for this result
     * @return
     */
    @Nonnull
    public JobExecutionStatus getStatus() {
        return status;
    }

    /**
     * Sets the <code>JobExecutionStatus</code> for this result
     * @param status the value to apply to this result
     */
    public void setStatus(JobExecutionStatus status) {
        this.status = status;
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

    /**
     * Wait for the Job to finish running.  This method will block until the Job finishes or until the
     * timeout duration is met.
     *
     * @param timeoutMillis the maximum duration in milliseconds to wait for the Job to complete
     * @throws InterruptedException if the Thread was interrupted while waiting
     */
    public void await(long timeoutMillis) throws InterruptedException {
        synchronized (this) {
            this.wait(timeoutMillis);
        }
    }

    /**
     * Takes the value from the provided <Code>JobResult</Code> and applies them to this instance.  All values
     * except the jobId will be copied
     * @param sourceResult the JobResult whose values will be copied
     */
    public void copy(JobResult sourceResult) {
        this.status = sourceResult.status;
        this.failedMessage = sourceResult.failedMessage;
        notify();
    }

}
