package org.csea.job;

import java.util.List;

/**
 * Interface the defines the contract with the service that knows how to
 * process jobs in batches
 */
public interface BatchProcessor {

    /**
     * Process a <code>List</code> of <code>Job</code>s
     * @param jobs the jobs to process
     * @return the results of each job
     */
    List<JobResult> process(List<Job> jobs);

}
