ARG DOCKER_REGISTRY=docker.1ms.run
FROM ${DOCKER_REGISTRY}/library/maven:3.9-eclipse-temurin-17 AS deps
WORKDIR /build
COPY settings.xml /tmp/maven-settings.xml
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B -s /tmp/maven-settings.xml

FROM ${DOCKER_REGISTRY}/library/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY settings.xml /tmp/maven-settings.xml
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -B -s /tmp/maven-settings.xml

FROM ${DOCKER_REGISTRY}/library/eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8" \
    LANG=C.UTF-8
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
