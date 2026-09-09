FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y --no-install-recommends maven \
    && mvn -q -DskipTests package \
    && apt-get purge -y maven && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd --system --create-home medibridge
COPY --from=build /app/target/medibridge-api-1.0.0.jar app.jar
USER medibridge
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/app.jar"]
