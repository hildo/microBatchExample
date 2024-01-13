package org.csea.job;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobExecutionService {

    private int batchSize;
    private BatchProcessor batchProcessor;

    private long maxDelayInMillis;

    private long lastRun = System.currentTimeMillis();

    private final List<PendingDetails> pendingJobs = new java.util.ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public JobExecutionService(int batchSize, @Nonnull BatchProcessor batchProcessor) {
        this(batchSize, 500, batchProcessor);
    }

    public JobExecutionService(int batchSize, long maxDelayInMillis, @Nonnull BatchProcessor batchProcessor) {
        this.batchSize = batchSize;
        this.batchProcessor = batchProcessor;
        this.maxDelayInMillis = maxDelayInMillis;

        scheduler.scheduleAtFixedRate(this::checkPending, maxDelayInMillis, maxDelayInMillis, TimeUnit.MILLISECONDS);
    }

    public JobResult submit(Job job) {
        JobResult result = new JobResult(job.getId());
        synchronized (pendingJobs) {
            pendingJobs.add(new PendingDetails(job, result));
        }
        scheduler.submit(this::checkPending);
        return result;
    }

    public void shutdown() {
        scheduler.shutdown();
        while (!pendingJobs.isEmpty()) {
            sendNextBatch();
        }
    }

    private void checkPending() {
        if (pendingJobs.isEmpty()) {
            // do nothing
            return;
        }

        if ((pendingJobs.size() == batchSize) || isTimeToSend()) {
            sendNextBatch();
        }
    }

    private void sendNextBatch() {
        sendJobs(nextJobs());
    }

    private boolean isTimeToSend() {
        return (lastRun + maxDelayInMillis < System.currentTimeMillis());
    }

    private List<PendingDetails> nextJobs() {
        List<PendingDetails> returnValue = new java.util.ArrayList<>();
        synchronized (pendingJobs) {
            while ((returnValue.size() < batchSize) && !pendingJobs.isEmpty()) {
                PendingDetails details = pendingJobs.getFirst();
                pendingJobs.remove(details);
                returnValue.add(details);
            }
        }
        return returnValue;
    }

    private void sendJobs(List<PendingDetails> jobDetails) {
        jobDetails.stream().map(details -> details.jobResult).forEach(result -> result.setStatus(JobExecutionStatus.RUNNING));
        List<Job> pendingJobs = jobDetails.stream().map(PendingDetails::getJob).toList();
        List<JobResult> results = batchProcessor.process(pendingJobs);
        lastRun = System.currentTimeMillis();
        for (JobResult result : results) {
            Optional<JobResult> resultToUpdateOpt = jobDetails.stream()
                    .filter(details -> details.job.getId().equals(result.getJobId()))
                    .map(details -> details.jobResult)
                    .findFirst();

            resultToUpdateOpt.ifPresent(resultToUpdate -> {
                synchronized (resultToUpdate) {
                    resultToUpdate.copy(result);
                    resultToUpdate.notify();
                }
            });
        }
    }

    private class PendingDetails {
        private Job job;
        private JobResult jobResult;

        private PendingDetails(Job job, JobResult jobResult) {
            this.job = job;
            this.jobResult = jobResult;
        }

        public Job getJob() {
            return job;
        }
    }
}
