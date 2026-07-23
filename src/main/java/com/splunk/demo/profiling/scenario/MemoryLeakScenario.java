package com.splunk.demo.profiling.scenario;

import com.splunk.demo.profiling.config.ScenarioProperties;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public final class MemoryLeakScenario implements DemoScenario {

    private static final String ID = "memory-leak";
    private static final int BYTES_PER_MEBIBYTE = 1024 * 1024;

    private final Object lifecycleMonitor = new Object();
    private final Object retainedObjectsMonitor = new Object();
    private final ScenarioProperties.MemoryLeak properties;
    private final List<byte[]> retainedObjects = new ArrayList<>();

    private volatile boolean enabled;
    private volatile Thread allocationThread;

    public MemoryLeakScenario(ScenarioProperties properties) {
        this.properties = properties.getMemoryLeak();
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void setEnabled(boolean requestedState) {
        synchronized (lifecycleMonitor) {
            if (requestedState == enabled) {
                return;
            }

            if (requestedState) {
                startAllocating();
            } else {
                stopAllocatingAndReleaseMemory();
            }
        }
    }

    private void startAllocating() {
        validateConfiguration();
        enabled = true;
        allocationThread = new Thread(this::retainAllocationsContinuously, "profiling-demo-memory-leak");
        allocationThread.setDaemon(true);
        allocationThread.start();
    }

    private void retainAllocationsContinuously() {
        while (enabled && !Thread.currentThread().isInterrupted()) {
            retainNextAllocation();
            try {
                Thread.sleep(properties.getAllocationIntervalMillis());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void retainNextAllocation() {
        synchronized (retainedObjectsMonitor) {
            int retainedMegabytes = retainedObjects.size() * properties.getAllocationMegabytes();
            if (retainedMegabytes + properties.getAllocationMegabytes()
                    > properties.getMaxRetainedMegabytes()) {
                return;
            }

            int allocationBytes = Math.multiplyExact(
                    properties.getAllocationMegabytes(),
                    BYTES_PER_MEBIBYTE);
            retainedObjects.add(new byte[allocationBytes]);
        }
    }

    private void stopAllocatingAndReleaseMemory() {
        enabled = false;
        Thread thread = allocationThread;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(1_000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        allocationThread = null;

        synchronized (retainedObjectsMonitor) {
            retainedObjects.clear();
        }
    }

    private void validateConfiguration() {
        if (properties.getAllocationMegabytes() < 1 || properties.getAllocationMegabytes() > 16) {
            throw new IllegalStateException("Allocation size must be between 1 and 16 MiB");
        }
        if (properties.getAllocationIntervalMillis() < 25
                || properties.getAllocationIntervalMillis() > 60_000) {
            throw new IllegalStateException("Allocation interval must be between 25 and 60000 ms");
        }
        if (properties.getMaxRetainedMegabytes() < properties.getAllocationMegabytes()
                || properties.getMaxRetainedMegabytes() > 512) {
            throw new IllegalStateException(
                    "Maximum retained memory must fit at least one allocation and cannot exceed 512 MiB");
        }
    }

    @Override
    public ScenarioStatus status() {
        int allocationCount;
        int retainedMegabytes;
        synchronized (retainedObjectsMonitor) {
            allocationCount = retainedObjects.size();
            retainedMegabytes = allocationCount * properties.getAllocationMegabytes();
        }

        return new ScenarioStatus(
                ID,
                "Memory leak",
                "Continuously retains allocations up to a configured safety limit.",
                enabled,
                Map.of(
                        "allocationCount", allocationCount,
                        "retainedMegabytes", retainedMegabytes,
                        "maxRetainedMegabytes", properties.getMaxRetainedMegabytes(),
                        "allocationMegabytes", properties.getAllocationMegabytes()));
    }

    @PreDestroy
    void shutdown() {
        setEnabled(false);
    }
}
