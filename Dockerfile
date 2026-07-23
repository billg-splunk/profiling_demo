FROM alpine:3.23 AS splunk-agent
ARG SPLUNK_OTEL_JAVA_AGENT_VERSION=2.29.0
RUN wget --quiet \
    "https://github.com/signalfx/splunk-otel-java/releases/download/v${SPLUNK_OTEL_JAVA_AGENT_VERSION}/splunk-otel-javaagent.jar" \
    --output-document=/splunk-otel-javaagent.jar

FROM maven:3.9.16-eclipse-temurin-25-alpine AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn --batch-mode dependency:go-offline

COPY src src
RUN mvn --batch-mode verify

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S profiling && adduser -S profiling -G profiling
COPY --from=build /workspace/target/profiling-demo-1.0.0.jar app.jar
COPY --from=splunk-agent /splunk-otel-javaagent.jar /opt/splunk/splunk-otel-javaagent.jar

USER profiling
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java", "-javaagent:/opt/splunk/splunk-otel-javaagent.jar", "-jar", "/app/app.jar"]
