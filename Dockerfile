FROM maven:3.6-openjdk-11-slim as builder
ADD src /usr/src/app/src
ADD pom.xml /usr/src/app
WORKDIR /usr/src/app
RUN mvn clean install

FROM openjdk:11-jre-slim
LABEL org.opencontainers.image.source https://github.com/SENERGY-Platform/event-operator-math-interval
ENV NAME event-math-interval
COPY --from=builder /usr/src/app/target/${NAME}-jar-with-dependencies.jar /opt/operator.jar
ADD https://github.com/jmxtrans/jmxtrans-agent/releases/download/jmxtrans-agent-1.2.6/jmxtrans-agent-1.2.6.jar opt/jmxtrans-agent.jar
CMD ["java","-jar","/opt/operator.jar"]
