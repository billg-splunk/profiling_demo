# Application and Observability Demo Specification

## 1. Document purpose

This document defines the application that will be used in a staged demonstration:

1. Define the application and demo story.
2. Build and verify the uninstrumented application with Codex.
3. Instrument the application for Splunk Observability Cloud.
4. Run the demo and confirm that the expected telemetry is visible.

Text marked **`[TO DEFINE]`** is intentionally left for the demo author to complete.

## 2. Application summary

### Working name

**`profiling_demo`**

### One-sentence description

**A simple app that shows profiling scenarios that will show the symptoms in Splunk Observability Cloud's Always-On Profiling feature.**

### Demo objective

**The user will be able to toggle the different scenarios and then see the results in Splunk Observability Cloud.**

### Intended audience

**Customers that are Java developers, Platform Engineers, SREs, etc..**

### Success statement

At the end of the demo, the audience should be able to:

- **Toggle the application with the different scenarios**
- **Upon toggling, the application should show the condition described**

## 3. Demo script

### Scenario

**As a Developer/SRE/Platform Engineer/etc., I need to be able to uncover various problematic situations with my code. Splunk Observability Cloud can help surface situations like blocked threads.**

### Preconditions

- I have an environment running docker, so I can easily deploy the application with docker and an otel collector running along side it.
- The application should be built using Splunk's distribution of Java instrumentation, with Always-On Profiling configured and enabled.
- The application has been built and its automated checks pass.
- For the instrumented portion, the environment can send telemetry to Splunk Observability Cloud but through Splunk's distribution of the Open Telemetry Collector.

### Demo Script

| Step | Presenter action | Expected application behavior | Expected observable result |
|---|---|---|---|
| 1 | Start Application | Application runs fine | I can see call stacks, but they represent normal behavior |
| 2 | Toggle `blocking threads` | Application shows threads blocking | I can see call stacks that show the blocking behavior |
| 3 | Toggle `memory leak` | Application shows a memory leak | I can see call stacks with the memory leak behavior |
| 4 | Untoggle any scenario | Each scenario goes back to normal state | I see normal behavior in the call stacks |

## 4. Architecture

This section is the primary place to define or revise the application architecture.

### Initial architecture choice

- A single Java application.
- One runnable process.
- A small HTTP API with a health endpoint and a few demo-oriented operations.
- The application should just run the scenarios. We shouldn't need an external user to do something to see the situations.
- In-memory data.
- Maven for builds
- docker compose to run the application and the open telemetry collector
- Automated unit tests and a small set of API-level tests.

### Technology decisions

| Decision | Selection | Rationale |
|---|---|---|
| Java version | Java 25 (current LTS) | Current stable LTS release with production-ready container images |
| Application framework | Spring Boot 4.1.0 | Current stable Spring Boot release with Java 25 support |
| Build tool | Maven | Maven is easy |
| Data storage | In-memory | This is just a demo, we don't need to save data |
| External dependencies | None | Don't need dependencies |
| Packaging | Runnable JAR, then packaged into docker | Easiest |
| Runtime environment | Docker | Keep it simple |

### Constraints

- Keep the first implementation small enough to explain during a live demo.
- Business behavior must be testable without Splunk Observability Cloud access.
- Instrumentation must not be required for the application to start or function.
- Secrets and access tokens must be provided at runtime and must not be committed.

## 5. Functional requirements

Assign stable identifiers so the implementation and demo script can refer to the same behavior.

| ID | Requirement | Priority | Acceptance notes |
|---|---|---|---|
| FR-001 | Provide an application health check. | Must | Returns a clear healthy response when the app is ready. |
| FR-002 | Provide a safe way to trigger any demo error or degraded behavior. | Must | The behavior is explicit, repeatable, and disabled or constrained outside the demo. |

## 7. Non-functional requirements

### Reliability and behavior

- The application starts with one documented command.
- A simple web interface is used to toggle scenarios
- Health and readiness are easy to verify.
- Expected errors return consistent, useful responses.
- Demo behavior is deterministic or has documented tolerances.

### Performance

- Any artificial latency must be clearly isolated and configurable.

### Security

- No credentials or tokens are stored in source control.
- Inputs are validated at system boundaries.

### Portability

- Required Java and build-tool versions are documented, and should all be handled within the docker build process.

## 8. Splunk Observability Cloud instrumentation plan

Instrumentation is a separate implementation phase. Exact dependencies and configuration should be selected when the architecture and runtime are confirmed.

### Telemetry goals

| Signal | Required | What the demo should reveal |
|---|---|---|
| Traces | Yes | Request flow, duration, errors, and dependency calls |
| Metrics | Yes | Request rate, error rate, latency, and runtime health |
| Logs | No | Application events correlated with trace context where supported |
| Profiling | Yes | Code-level CPU or memory behavior relevant to the demo |

### Resource identity

Define stable values before instrumenting:

| Attribute | Value |
|---|---|
| Service name | profiling-demo |
| Service version | 1.0 |
| Deployment environment | demo |

### Instrumentation approach

Prefer automatic instrumentation for standard framework and HTTP behavior. Add manual spans, metrics, or attributes only when they make an important part of the demo story visible.

### Telemetry acceptance criteria

- The service is discoverable under its agreed service name.
- A demo request produces an end-to-end trace.
- Errors are distinguishable from successful requests.
- Key latency is attributed to a useful span or dependency.
- Required metrics appear with the expected environment and service dimensions.
- Logs or profiling data appear if those signals are in scope.
- No secrets or sensitive user data appear in telemetry.

## 9. Delivery phases

### Phase 1: Complete the specification

- Fill in the application summary and demo script.
- Confirm the architecture and technology decisions.
- Define functional requirements and acceptance criteria.
- Choose which telemetry signals are in scope.

### Phase 2: Build the application

- Scaffold the Java project.
- Implement the functional requirements.
- Add automated tests.
- Document local build, test, and run commands.
- Validate the uninstrumented demo flow.

Exit criterion: the demo script succeeds locally without observability instrumentation.

### Phase 3: Instrument the application

- Select the Splunk/OpenTelemetry integration for the confirmed runtime.
- Add runtime configuration and safe configuration examples.
- Add only the manual instrumentation needed by the demo story.
- Confirm telemetry export and resource identity.

Exit criterion: telemetry acceptance criteria are met.

### Phase 4: Rehearse and package the demo

- Run the final script from a clean state.
- Verify expected telemetry in Splunk Observability Cloud.
- Record timing, reset, troubleshooting, and recovery steps.
- Confirm no secrets or environment-specific values are committed.

Exit criterion: another presenter can reproduce the demo using repository documentation.

## 10. Verification checklist

- [ ] Application purpose and audience are defined.
- [ ] Demo walkthrough is complete.
- [ ] Architecture choices are confirmed.
- [ ] Functional requirements have acceptance notes.
- [ ] Build, test, and run commands are documented.
- [ ] The application works without instrumentation.
- [ ] Telemetry goals and resource attributes are defined.
- [ ] Splunk Observability Cloud integration is verified.
- [ ] Reset and troubleshooting procedures are documented.
- [ ] No credentials, tokens, or sensitive telemetry are committed.

## 11. Definition of done

The work is complete when:

1. The application implements all Must requirements.
2. Automated checks pass from a clean checkout.
3. The uninstrumented and instrumented demo flows are documented and repeatable.
4. Required telemetry is visible and correctly identified in Splunk Observability Cloud.
5. The final demo script, reset steps, and troubleshooting guidance have been rehearsed.
