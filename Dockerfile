# 多阶段构建：使用 Maven 镜像打包，再使用 JRE 镜像运行
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /data
COPY --from=builder /build/target/logistics-item-lending-api.jar /app/app.jar
EXPOSE 8011
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
