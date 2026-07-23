package com.splunk.demo.profiling.web;

import com.splunk.demo.profiling.scenario.ScenarioService;
import com.splunk.demo.profiling.scenario.ScenarioStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private final ScenarioService scenarioService;

    public ScenarioController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @GetMapping
    public List<ScenarioStatus> list() {
        return scenarioService.list();
    }

    @PutMapping("/{scenarioId}")
    public ScenarioStatus setEnabled(
            @PathVariable String scenarioId,
            @Valid @RequestBody ToggleScenarioRequest request) {
        return scenarioService.setEnabled(scenarioId, request.enabled());
    }

    public record ToggleScenarioRequest(@NotNull Boolean enabled) {
    }
}
