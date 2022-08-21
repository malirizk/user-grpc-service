FROM adoptopenjdk/openjdk11:jdk-11.0.2.9-slim
WORKDIR /opt
ENV PORT 9898
EXPOSE 9898
COPY build/*.jar /opt/user-grpc-server.jar
ENTRYPOINT exec java $JAVA_OPTS -Dspring.profiles.active=dev -jar user-grpc-server.jar