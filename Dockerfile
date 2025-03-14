# Build phase
FROM bellsoft/liberica-openjdk-alpine:17 AS build

# Add Maintainer Info
LABEL maintainer="admin.apptemplate@gmail.com"
LABEL org.opencontainers.image.description="Springboot3 API Service"

# Create a working directory
WORKDIR /app

# Copy the source code to the working directory
COPY . .

# Grant execute permission to mvnw
RUN chmod +x ./mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Start with a base image containing Java runtime
FROM bellsoft/liberica-openjre-alpine:17

# Environment Variables
# not used yet in the application, but can be to detect env mode
ENV TEST_ENV="DEV"

# Create a working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/springboot3_api*.jar app.jar

# Make port 8080 available to the world outside this container (or 8443)
EXPOSE 8080 

# ex The application's jar file with versioning
#ARG VERSION
#ARG JAR_FILE=build/libs/api-${VERSION}.jar

# ex Copy the application's jar to the working directory
# ADD ${JAR_FILE} app.jar
#COPY ${JAR_FILE} app.jar

# ex Run a build command to update packages
#Debian
#RUN apt-get update 
#Alpine
#RUN apk update

# For civilized shutdown, not there yet
# STOPSIGNAL SIGQUIT

# Not used , to create anonymous volumes
# better to mount externally when deploying with the -v param to point to an external path
#VOLUME /external_data

# to run the jar in the background (messes up PID1)
# CMD exec java -jar /app/app.jar &

# Run as a non-root user
# Debian
#RUN useradd -m user1
# Alpine
RUN adduser -D user1
USER user1

# Health Check between docker and the container, using the health actuator
HEALTHCHECK CMD curl --fail http://localhost:8080/actuator/health || exit 1

# Run the jar file on execution in the foreground
ENTRYPOINT ["java","-jar","/app/app.jar"]
