package com.splunk.demo.profiling.scenario;

import java.util.Map;

public record ScenarioStatus(
        String id,
        String displayName,
        String description,
        boolean enabled,
        Map<String, Object> details) {
}
