package org.csea.job;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.*;


public class JobExecutionServiceTest {

    @Test
    public void testServiceSingleBatchSizeSuccess() throws InterruptedException {
        TestJob testJob = new TestJob();

        BatchProcessor mockedBatchProcessor = mock(BatchProcessor.class);
        when(mockedBatchProcessor.process(anyList())).thenReturn(Arrays.asList(new JobResult(testJob.getId(), JobExecutionStatus.SUCCESS)));

        JobExecutionService service = new JobExecutionService(1, mockedBatchProcessor);
        JobResult jobResult = service.submit(testJob);
        Assertions.assertNotNull(jobResult);
        int counter = 0;
        while (!jobResult.getStatus().isComplete() && counter++ < 4) {

            jobResult.await(1000);
        }
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult.getStatus());
        verify(mockedBatchProcessor).process(any());
        verifyNoMoreInteractions(mockedBatchProcessor);
    }

    private static class TestJob implements Job {

        private UUID id = UUID.randomUUID();

        @Override
        public UUID getId() {
            return id;
        }
    }
}
