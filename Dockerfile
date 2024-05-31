FROM openjdk:21

WORKDIR /app

COPY wait-for-it.sh /wait-for-it.sh

COPY build/libs/transaciton-service-1.0.0.jar /app

ENTRYPOINT ["/wait-for-it.sh", "db:5432", "--", "java", "-jar", "transaciton-service-1.0.0.jar"]