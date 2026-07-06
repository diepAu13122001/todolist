# ---- Stage 1: build the jar with Maven ----
FROM eclipse-temurin:23-jdk AS build
WORKDIR /app

# Copy the whole project and build. Skip tests to keep the deploy build fast.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
RUN chmod +x mvnw && ./mvnw -q clean package -DskipTests

# ---- Stage 2: run only the jar on a small JRE image ----
FROM eclipse-temurin:23-jre
WORKDIR /app

# Copy the built jar from the build stage.
COPY --from=build /app/target/*.jar app.jar

# Folder for the H2 file database (attach a Railway volume here to keep data).
RUN mkdir -p /app/data

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
