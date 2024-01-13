# Micro-batching library
This library was written in Java.  The only third party dependencies used are for
testing (JUnit, Mockito).

## Job Execution Service
Classes can create instances of the [JobExecutionService](src/main/java/org/csea/job/JobExecutionService.java)

```
 public JobExecutionService(int batchSize, @Nonnull BatchProcessor batchProcessor)
 public JobExecutionService(int batchSize, long maxDelayInMillis, @Nonnull BatchProcessor batchProcessor)
```
An implementation of the [BatchProcessor](src/main/java/org/csea/job/BatchProcessor.java) interface must be provided to the service's constructor.
The batchSize parameter configures the number of jobs that will be sent to
the processor in one call.  Optionally, the maxDelayInMillis will define how long until
a batch of Pending Jobs wll be sent to the processor even if the batch size is not met.

## Job Definition
A job to be submitted to the processor via the JobExecutionService must implement the
[Job](src/main/java/org/csea/job/Job.java) interface.

## Job Submission
Job instances can be submitted to the service using the submit method

```java
public JobResult submit(Job job)
```
For each submitted Job, a JobResult will be provided.  Callers submitting Jobs can
use the Result to determine the [status](src/main/java/org/csea/job/JobExecutionStatus.java) of the Job.  A jobs status wll be one of four values
- PENDING: The job has been submitted but not sent to the processor
- RUNNING: The job has been sent to the processor but a response has not been received
- SUCCESS: The batch processor reported a successful job execution
- FAIL: The batch processor reported an unsuccessful job execution

The caller can inspect the Status to determine the outcome of the Job.  If the caller wants to be notified when
the Job has been processed, they can use the await method, which will block the calling thread until the Job
has been processed.  The await method accepts a parameter that defined how long to wait in milliseconds.  For example

```java
TestJob testJob = new TestJob(); //implements the Job interface

JobResult jobResult = jobExecutionService.submit(testJob);
try {
    jobResult.await(1000); // waiting one second
} catch (InterruptedException ex) {
    // The wait was interrupted
}

if (jobResult.getStatus() == JobExecutionStatus.SUCCESS) {
        ...
}

```

## Job Service Shutdown
The shutdown method should be used when the service is no longer needed.  It will ensure that any pending Jobs will
be submitted to the Processor before returning control to the caller.

```
public void shutdown()
```