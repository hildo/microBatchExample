package org.csea.job;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
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

    @Test
    public void testServiceSingleBatchSizeFail() throws InterruptedException {
        TestJob testJob = new TestJob();

        BatchProcessor mockedBatchProcessor = mock(BatchProcessor.class);
        when(mockedBatchProcessor.process(anyList())).thenReturn(Arrays.asList(new JobResult(testJob.getId(), JobExecutionStatus.FAIL, "Bad things happened")));

        JobExecutionService service = new JobExecutionService(1, mockedBatchProcessor);
        JobResult jobResult = service.submit(testJob);
        Assertions.assertNotNull(jobResult);
        int counter = 0;
        while (!jobResult.getStatus().isComplete() && counter++ < 4) {

            jobResult.await(1000);
        }
        Assertions.assertEquals(JobExecutionStatus.FAIL, jobResult.getStatus());
        Assertions.assertEquals("Bad things happened", jobResult.getFailedMessage());
        verify(mockedBatchProcessor).process(any());
        verifyNoMoreInteractions(mockedBatchProcessor);
    }

    @Test
    public void testServiceThreeBatchSizeSuccess() throws InterruptedException {
        TestJob testJob1 = new TestJob();
        TestJob testJob2 = new TestJob();
        TestJob testJob3 = new TestJob();

        BatchProcessor mockedBatchProcessor = mock(BatchProcessor.class);
        when(mockedBatchProcessor.process(anyList())).thenReturn(Arrays.asList(
                new JobResult(testJob1.getId(), JobExecutionStatus.SUCCESS),
                new JobResult(testJob2.getId(), JobExecutionStatus.SUCCESS),
                new JobResult(testJob3.getId(), JobExecutionStatus.SUCCESS)
            )
        );

        JobExecutionService service = new JobExecutionService(3, mockedBatchProcessor);
        JobResult jobResult1 = service.submit(testJob1);
        Assertions.assertNotNull(jobResult1);
        JobResult jobResult2 = service.submit(testJob2);
        Assertions.assertNotNull(jobResult2);
        JobResult jobResult3 = service.submit(testJob3);
        Assertions.assertNotNull(jobResult3);
        int counter = 0;
        while (!(
            jobResult1.getStatus().isComplete() &&
            jobResult2.getStatus().isComplete() &&
            jobResult3.getStatus().isComplete()
        )
        && counter++ < 4) {

            jobResult3.await(1000);
        }
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult1.getStatus());
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult2.getStatus());
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult3.getStatus());
        verify(mockedBatchProcessor).process(any());
        verifyNoMoreInteractions(mockedBatchProcessor);
    }

    @Test
    public void testServiceThreeBatchSizeNotAllSuccess() throws InterruptedException {
        TestJob testJob1 = new TestJob();
        TestJob testJob2 = new TestJob();
        TestJob testJob3 = new TestJob();

        BatchProcessor mockedBatchProcessor = mock(BatchProcessor.class);
        when(mockedBatchProcessor.process(anyList())).thenReturn(Arrays.asList(
                        new JobResult(testJob1.getId(), JobExecutionStatus.SUCCESS),
                        new JobResult(testJob2.getId(), JobExecutionStatus.FAIL),
                        new JobResult(testJob3.getId(), JobExecutionStatus.SUCCESS)
                )
        );

        JobExecutionService service = new JobExecutionService(3, mockedBatchProcessor);
        JobResult jobResult1 = service.submit(testJob1);
        Assertions.assertNotNull(jobResult1);
        JobResult jobResult2 = service.submit(testJob2);
        Assertions.assertNotNull(jobResult2);
        JobResult jobResult3 = service.submit(testJob3);
        Assertions.assertNotNull(jobResult3);
        int counter = 0;
        while (!(
                jobResult1.getStatus().isComplete() &&
                        jobResult2.getStatus().isComplete() &&
                        jobResult3.getStatus().isComplete()
        )
                && counter++ < 4) {

            jobResult3.await(1000);
        }
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult1.getStatus());
        Assertions.assertEquals(JobExecutionStatus.FAIL, jobResult2.getStatus());
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult3.getStatus());
        verify(mockedBatchProcessor).process(any());
        verifyNoMoreInteractions(mockedBatchProcessor);
    }

    @Test
    public void testServiceOneBatchSizeThreeRequestSuccess() throws InterruptedException {
        TestJob testJob1 = new TestJob();
        TestJob testJob2 = new TestJob();
        TestJob testJob3 = new TestJob();

        BatchProcessor mockedBatchProcessor = mock(BatchProcessor.class);
        when(mockedBatchProcessor.process(eq(List.of(testJob1)))).thenReturn(List.of(new JobResult(testJob1.getId(), JobExecutionStatus.SUCCESS)));
        when(mockedBatchProcessor.process(eq(List.of(testJob2)))).thenReturn(List.of(new JobResult(testJob2.getId(), JobExecutionStatus.SUCCESS)));
        when(mockedBatchProcessor.process(eq(List.of(testJob3)))).thenReturn(List.of(new JobResult(testJob3.getId(), JobExecutionStatus.SUCCESS)));

        JobExecutionService service = new JobExecutionService(1, mockedBatchProcessor);
        JobResult jobResult1 = service.submit(testJob1);
        Assertions.assertNotNull(jobResult1);
        JobResult jobResult2 = service.submit(testJob2);
        Assertions.assertNotNull(jobResult2);
        JobResult jobResult3 = service.submit(testJob3);
        Assertions.assertNotNull(jobResult3);
        int counter = 0;
        while (!(
                jobResult1.getStatus().isComplete() &&
                        jobResult2.getStatus().isComplete() &&
                        jobResult3.getStatus().isComplete()
        )
                && counter++ < 4) {

            jobResult3.await(1000);
        }
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult1.getStatus());
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult2.getStatus());
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult3.getStatus());
        verify(mockedBatchProcessor, times(3)).process(any());
        verifyNoMoreInteractions(mockedBatchProcessor);
    }

    @Test
    public void testServiceTwoBatchSizeThreeRequestSuccess() throws InterruptedException {
        TestJob testJob1 = new TestJob();
        TestJob testJob2 = new TestJob();
        TestJob testJob3 = new TestJob();

        BatchProcessor mockedBatchProcessor = mock(BatchProcessor.class);
        when(mockedBatchProcessor.process(eq(List.of(testJob1, testJob2)))).thenReturn(
                List.of(
                    new JobResult(testJob1.getId(), JobExecutionStatus.SUCCESS),
                    new JobResult(testJob2.getId(), JobExecutionStatus.SUCCESS)
                )
        );
        when(mockedBatchProcessor.process(eq(List.of(testJob3)))).thenReturn(List.of(new JobResult(testJob3.getId(), JobExecutionStatus.SUCCESS)));

        JobExecutionService service = new JobExecutionService(2, mockedBatchProcessor);
        JobResult jobResult1 = service.submit(testJob1);
        Assertions.assertNotNull(jobResult1);
        JobResult jobResult2 = service.submit(testJob2);
        Assertions.assertNotNull(jobResult2);
        JobResult jobResult3 = service.submit(testJob3);
        Assertions.assertNotNull(jobResult3);
        int counter = 0;
        while (!(
                jobResult1.getStatus().isComplete() &&
                        jobResult2.getStatus().isComplete() &&
                        jobResult3.getStatus().isComplete()
        )
                && counter++ < 4) {
            jobResult3.await(1000);
        }
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult1.getStatus());
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult2.getStatus());
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult3.getStatus());
        verify(mockedBatchProcessor, times(2)).process(any());
        verifyNoMoreInteractions(mockedBatchProcessor);
    }

    @Test
    public void testShutdown() {
        TestJob testJob1 = new TestJob();
        TestJob testJob2 = new TestJob();
        TestJob testJob3 = new TestJob();

        BatchProcessor mockedBatchProcessor = mock(BatchProcessor.class);
        when(mockedBatchProcessor.process(eq(List.of(testJob1, testJob2, testJob3)))).thenReturn(
                List.of(
                        new JobResult(testJob1.getId(), JobExecutionStatus.SUCCESS),
                        new JobResult(testJob2.getId(), JobExecutionStatus.SUCCESS),
                        new JobResult(testJob3.getId(), JobExecutionStatus.SUCCESS)
                )
        );
        when(mockedBatchProcessor.process(eq(List.of(testJob3)))).thenReturn(List.of(new JobResult(testJob3.getId(), JobExecutionStatus.SUCCESS)));

        JobExecutionService service = new JobExecutionService(4, mockedBatchProcessor);
        JobResult jobResult1 = service.submit(testJob1);
        Assertions.assertNotNull(jobResult1);
        Assertions.assertEquals(JobExecutionStatus.PENDING, jobResult1.getStatus());
        JobResult jobResult2 = service.submit(testJob2);
        Assertions.assertNotNull(jobResult2);
        Assertions.assertEquals(JobExecutionStatus.PENDING, jobResult2.getStatus());
        JobResult jobResult3 = service.submit(testJob3);
        Assertions.assertNotNull(jobResult3);
        Assertions.assertEquals(JobExecutionStatus.PENDING, jobResult3.getStatus());

        service.shutdown();

        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult1.getStatus());
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult2.getStatus());
        Assertions.assertEquals(JobExecutionStatus.SUCCESS, jobResult3.getStatus());
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
