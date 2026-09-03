package com.cadw.automation.bdd.hooks;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class ScenarioExecutionGuard {
    private static final ReentrantReadWriteLock EXECUTION_LOCK =
            new ReentrantReadWriteLock(true);
    private static final ThreadLocal<Lock> HELD_LOCK = new ThreadLocal<>();

    private ScenarioExecutionGuard() {
    }

    static void acquire(boolean exclusive) {
        Lock lock = exclusive ? EXECUTION_LOCK.writeLock() : EXECUTION_LOCK.readLock();
        lock.lock();
        HELD_LOCK.set(lock);
    }

    static void release() {
        Lock lock = HELD_LOCK.get();
        if (lock == null) {
            return;
        }
        try {
            lock.unlock();
        } finally {
            HELD_LOCK.remove();
        }
    }
}
