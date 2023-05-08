FROM eclipse-temurin:17-alpine

WORKDIR /app
ADD build/libs/slowwave-*.jar /app/slowwave.jar

EXPOSE 1234 8080

ENTRYPOINT ["java", "-jar", "/app/slowwave.jar"]
CMD ["--help"]

