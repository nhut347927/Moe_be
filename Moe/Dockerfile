FROM openjdk:17-jdk-slim

# Cài đặt ffmpeg
RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*

# Tạo thư mục app và copy file JAR
WORKDIR /app
COPY target/social-network.jar app.jar

# Port ứng dụng
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "moe.jar"]
