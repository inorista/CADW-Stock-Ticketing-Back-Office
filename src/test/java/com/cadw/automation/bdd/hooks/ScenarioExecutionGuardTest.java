package com.cadw.automation.bdd.hooks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ScenarioExecutionGuardTest {

    @Test(timeOut = 5000)
    public void exclusiveScenarioWaitsForRunningParallelScenario() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch parallelStarted = new CountDownLatch(1);
        CountDownLatch releaseParallel = new CountDownLatch(1);
        CountDownLatch exclusiveStarted = new CountDownLatch(1);
        try {
            Future<?> parallel = executor.submit(() -> {
                ScenarioExecutionGuard.acquire(false);
                try {
                    parallelStarted.countDown();
                    releaseParallel.await();
                } finally {
                    ScenarioExecutionGuard.release();
                }
                return null;
            });
            Assert.assertTrue(parallelStarted.await(1, TimeUnit.SECONDS));

            Future<?> exclusive = executor.submit(() -> {
                ScenarioExecutionGuard.acquire(true);
                try {
                    exclusiveStarted.countDown();
                } finally {
                    ScenarioExecutionGuard.release();
                }
            });

            Assert.assertFalse(
                    exclusiveStarted.await(150, TimeUnit.MILLISECONDS),
                    "Exclusive scenario must wait while a parallel scenario is running");
            releaseParallel.countDown();
            Assert.assertTrue(exclusiveStarted.await(1, TimeUnit.SECONDS));
            parallel.get();
            exclusive.get();
        } finally {
            releaseParallel.countDown();
            executor.shutdownNow();
        }
    }
}
