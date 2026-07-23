package com.splunk.demo.profiling.scenario;

import com.splunk.demo.profiling.config.ScenarioProperties;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public final class BlockingThreadsScenario implements DemoScenario {

    private static final String ID = "blocking-threads";

    private final Object lifecycleMonitor = new Object();
    private final Object contestedMonitor = new Object();
    private final ScenarioProperties.Blocking properties;
    private final List<Thread> scenarioThreads = new ArrayList<>();

    private volatile boolean enabled;

    public BlockingThreadsScenario(ScenarioProperties properties) {
        this.properties = properties.getBlocking();
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
                startWorkers();
            } else {
                stopWorkers();
            }
        }
    }

    private void startWorkers() {
        validateConfiguration();
        enabled = true;

        Thread lockOwner = new Thread(this::holdContestedMonitor, "profiling-demo-lock-owner");
        lockOwner.setDaemon(true);
        scenarioThreads.add(lockOwner);

        for (int index = 1; index <= properties.getWorkerCount(); index++) {
            Thread worker = new Thread(this::waitForContestedMonitor, "profiling-demo-blocked-" + index);
            worker.setDaemon(true);
            scenarioThreads.add(worker);
        }

        scenarioThreads.forEach(Thread::start);
    }

    private void holdContestedMonitor() {
        while (enabled && !Thread.currentThread().isInterrupted()) {
            synchronized (contestedMonitor) {
                if (!pause(properties.getLockHoldMillis())) {
                    return;
                }
            }
            if (!pause(properties.getGapMillis())) {
                return;
            }
        }
    }

    private void waitForContestedMonitor() {
        while (enabled && !Thread.currentThread().isInterrupted()) {
            intentionallyBlockOnSharedMonitor();
        }
    }

    private void intentionallyBlockOnSharedMonitor() {
        synchronized (contestedMonitor) {
            Thread.onSpinWait();
        }
    }

    private boolean pause(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void stopWorkers() {
        enabled = false;
        scenarioThreads.forEach(Thread::interrupt);

        for (Thread thread : scenarioThreads) {
            try {
                thread.join(1_000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        scenarioThreads.clear();
    }

    private void validateConfiguration() {
        if (properties.getWorkerCount() < 1 || properties.getWorkerCount() > 64) {
            throw new IllegalStateException("Blocking worker count must be between 1 and 64");
        }
        if (properties.getLockHoldMillis() < 1 || properties.getLockHoldMillis() > 30_000) {
            throw new IllegalStateException("Lock hold time must be between 1 and 30000 ms");
        }
        if (properties.getGapMillis() < 0 || properties.getGapMillis() > 30_000) {
            throw new IllegalStateException("Lock gap time must be between 0 and 30000 ms");
        }
    }

    @Override
    public ScenarioStatus status() {
        long activeThreadCount;
        synchronized (lifecycleMonitor) {
            activeThreadCount = scenarioThreads.stream().filter(Thread::isAlive).count();
        }

        return new ScenarioStatus(
                ID,
                "Blocking threads",
                "Creates monitor contention so worker threads remain visibly blocked.",
                enabled,
                Map.of(
                        "workerCount", properties.getWorkerCount(),
                        "activeThreadCount", activeThreadCount,
                        "lockHoldMillis", properties.getLockHoldMillis()));
    }

    @PreDestroy
    void shutdown() {
        setEnabled(false);
    }
}
