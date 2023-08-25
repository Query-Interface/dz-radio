FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /opt/app
COPY .mvn/ .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml
RUN /opt/app/mvnw dependency:go-offline
COPY ./src ./src
RUN /opt/app/mvnw clean install -DskipTests=true
 
FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app
EXPOSE 8080
COPY --from=builder /opt/app/target/*.jar /opt/app/*.jar
ENTRYPOINT ["java", "-jar", "/opt/app/*.jar" ]