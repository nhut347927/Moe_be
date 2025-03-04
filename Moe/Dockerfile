# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Install FFmpeg
RUN apt-get update && apt-get install -y ffmpeg

# Set the working directory in the container
WORKDIR /app

# Copy the project files to the container
COPY . /app

# Specify the command to run the application
CMD ["java", "-jar", "target/your-application.jar"]