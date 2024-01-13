package org.csea.job;

import java.util.UUID;

/**
 * A Job o be submitted to the processor
 */
public interface Job {

    /**
     * The unique identifier for this Job
     * @return a <code>UUID</code> instance
     */
    UUID getId();

}
