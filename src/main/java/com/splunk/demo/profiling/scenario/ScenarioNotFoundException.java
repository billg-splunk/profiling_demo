package com.splunk.demo.profiling.scenario;

public class ScenarioNotFoundException extends RuntimeException {

    public ScenarioNotFoundException(String scenarioId) {
        super("Unknown scenario: " + scenarioId);
    }
}
