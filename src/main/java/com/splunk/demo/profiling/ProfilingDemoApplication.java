package com.splunk.demo.profiling;

import com.splunk.demo.profiling.config.ScenarioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ScenarioProperties.class)
public class ProfilingDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfilingDemoApplication.class, args);
    }
}
