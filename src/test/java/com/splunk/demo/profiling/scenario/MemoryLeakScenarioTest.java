package com.splunk.demo.profiling.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import com.splunk.demo.profiling.config.ScenarioProperties;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MemoryLeakScenarioTest {

    private final ScenarioProperties properties = new ScenarioProperties();
    private final MemoryLeakScenario scenario = new MemoryLeakScenario(properties);

    @AfterEach
    void stopScenario() {
        scenario.setEnabled(false);
    }

    @Test
    void retainsMemoryUpToTheConfiguredLimitAndReleasesItWhenDisabled() {
        properties.getMemoryLeak().setAllocationMegabytes(2);
        properties.getMemoryLeak().setAllocationIntervalMillis(25);
        properties.getMemoryLeak().setMaxRetainedMegabytes(3);

        scenario.setEnabled(true);

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(scenario.status().details().get("retainedMegabytes"))
                        .isEqualTo(2));

        scenario.setEnabled(false);

        assertThat(scenario.status().enabled()).isFalse();
        assertThat(scenario.status().details().get("retainedMegabytes")).isEqualTo(0);
    }
}
