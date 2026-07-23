package com.splunk.demo.profiling.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "demo.scenarios")
public class ScenarioProperties {

    private final Blocking blocking = new Blocking();
    private final MemoryLeak memoryLeak = new MemoryLeak();

    public Blocking getBlocking() {
        return blocking;
    }

    public MemoryLeak getMemoryLeak() {
        return memoryLeak;
    }

    public static class Blocking {

        private int workerCount = 8;
        private long lockHoldMillis = 900;
        private long gapMillis = 50;

        public int getWorkerCount() {
            return workerCount;
        }

        public void setWorkerCount(int workerCount) {
            this.workerCount = workerCount;
        }

        public long getLockHoldMillis() {
            return lockHoldMillis;
        }

        public void setLockHoldMillis(long lockHoldMillis) {
            this.lockHoldMillis = lockHoldMillis;
        }

        public long getGapMillis() {
            return gapMillis;
        }

        public void setGapMillis(long gapMillis) {
            this.gapMillis = gapMillis;
        }
    }

    public static class MemoryLeak {

        private int allocationMegabytes = 1;
        private long allocationIntervalMillis = 250;
        private int maxRetainedMegabytes = 128;

        public int getAllocationMegabytes() {
            return allocationMegabytes;
        }

        public void setAllocationMegabytes(int allocationMegabytes) {
            this.allocationMegabytes = allocationMegabytes;
        }

        public long getAllocationIntervalMillis() {
            return allocationIntervalMillis;
        }

        public void setAllocationIntervalMillis(long allocationIntervalMillis) {
            this.allocationIntervalMillis = allocationIntervalMillis;
        }

        public int getMaxRetainedMegabytes() {
            return maxRetainedMegabytes;
        }

        public void setMaxRetainedMegabytes(int maxRetainedMegabytes) {
            this.maxRetainedMegabytes = maxRetainedMegabytes;
        }
    }
}
