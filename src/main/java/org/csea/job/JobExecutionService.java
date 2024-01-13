package org.csea.job;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class JobExecutionService {

    private int batchSize;
    private BatchProcessor batchProcessor;

    private final List<PendingDetails> pendingJobs = new java.util.ArrayList<>();

    public JobExecutionService(int batchSize, @Nonnull BatchProcessor batchProcessor) {
        this.batchSize = batchSize;
        this.batchProcessor = batchProcessor;
    }

    public JobResult submit(Job job) {
        JobResult result = new JobResult(job.getId());
        synchronized (pendingJobs) {
            pendingJobs.add(new PendingDetails(job, result));
        }
        checkPending();
        return result;
    }

    private void checkPending() {
        if (pendingJobs.size() == batchSize) {
            sendJobs(nextJobs());
        }
    }

    private List<PendingDetails> nextJobs() {
        List<PendingDetails> returnValue = new java.util.ArrayList<>();
        synchronized (pendingJobs) {
            while ((returnValue.size() < batchSize) && !pendingJobs.isEmpty()) {
                PendingDetails details = pendingJobs.get(0);
                pendingJobs.remove(details);
                returnValue.add(details);
            }
        }
        return returnValue;
    }

    private void sendJobs(List<PendingDetails> jobDetails) {
        List<Job> pendingJobs = jobDetails.stream().map(PendingDetails::getJob).toList();
        List<JobResult> results = batchProcessor.process(pendingJobs);
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
