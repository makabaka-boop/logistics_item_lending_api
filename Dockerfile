FROM maven:3.9.6-eclipse-temurin-11 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=builder /app/target/item-lending-api-1.0.0.jar app.jar

RUN mkdir -p /data

EXPOSE 8011

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
