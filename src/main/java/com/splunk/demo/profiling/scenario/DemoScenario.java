package com.splunk.demo.profiling.scenario;

public interface DemoScenario {

    String id();

    void setEnabled(boolean enabled);

    ScenarioStatus status();
}
