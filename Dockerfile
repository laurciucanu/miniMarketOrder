FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/market-order-service.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
