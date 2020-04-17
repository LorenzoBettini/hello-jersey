FROM openjdk:8

COPY /target/*.jar /app/app.jar
COPY /target/dependency /app/dependency

CMD ["java", "-cp", "/app/app.jar:/app/dependency/*", "com.examples.Main"]
