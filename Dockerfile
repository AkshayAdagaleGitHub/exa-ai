# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the project files to the container
COPY . .

# Add the Maven wrapper
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "target/mcp-server-0.0.1-SNAPSHOT.jar"]