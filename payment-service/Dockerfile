#FROM eclipse-temurin:17-jdk AS builder
#WORKDIR /app
#COPY mvnw .
#COPY .mvn .mvn
#COPY pom.xml .
#COPY src src
#COPY settings.xml /root/.m2/settings.xml
#RUN chmod +x mvnw
#RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
#COPY --from=builder /app/target/*.jar app.jar
COPY target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
