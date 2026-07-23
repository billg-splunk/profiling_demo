package com.splunk.demo.profiling.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import com.splunk.demo.profiling.config.ScenarioProperties;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BlockingThreadsScenarioTest {

    private final ScenarioProperties properties = new ScenarioProperties();
    private final BlockingThreadsScenario scenario = new BlockingThreadsScenario(properties);

    @AfterEach
    void stopScenario() {
        scenario.setEnabled(false);
    }

    @Test
    void startsAndStopsTheBoundedWorkerSet() {
        properties.getBlocking().setWorkerCount(3);
        properties.getBlocking().setLockHoldMillis(100);
        properties.getBlocking().setGapMillis(5);

        scenario.setEnabled(true);

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(scenario.status().details().get("activeThreadCount"))
                        .isEqualTo(4L));

        scenario.setEnabled(false);

        assertThat(scenario.status().enabled()).isFalse();
        assertThat(scenario.status().details().get("activeThreadCount")).isEqualTo(0L);
    }
}
