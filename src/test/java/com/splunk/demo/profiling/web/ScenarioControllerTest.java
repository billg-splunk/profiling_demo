package com.splunk.demo.profiling.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsTheAvailableScenarios() throws Exception {
        mockMvc.perform(get("/api/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("blocking-threads"))
                .andExpect(jsonPath("$[1].id").value("memory-leak"));
    }

    @Test
    void togglesAScenarioAndCanReturnItToNormal() throws Exception {
        mockMvc.perform(put("/api/scenarios/blocking-threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));

        mockMvc.perform(put("/api/scenarios/blocking-threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void returnsUsefulErrorsForUnknownScenariosAndInvalidRequests() throws Exception {
        mockMvc.perform(put("/api/scenarios/not-real")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Unknown scenario: not-real"));

        mockMvc.perform(put("/api/scenarios/memory-leak")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("The request must include a non-null 'enabled' boolean"));
    }
}
