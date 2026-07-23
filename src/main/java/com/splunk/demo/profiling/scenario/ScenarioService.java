package com.splunk.demo.profiling.scenario;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ScenarioService {

    private final Map<String, DemoScenario> scenarios;

    public ScenarioService(List<DemoScenario> discoveredScenarios) {
        this.scenarios = discoveredScenarios.stream()
                .sorted(Comparator.comparing(DemoScenario::id))
                .collect(Collectors.toMap(
                        DemoScenario::id,
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new));
    }

    public List<ScenarioStatus> list() {
        return scenarios.values().stream().map(DemoScenario::status).toList();
    }

    public ScenarioStatus setEnabled(String id, boolean enabled) {
        DemoScenario scenario = scenarios.get(id);
        if (scenario == null) {
            throw new ScenarioNotFoundException(id);
        }
        scenario.setEnabled(enabled);
        return scenario.status();
    }
}
