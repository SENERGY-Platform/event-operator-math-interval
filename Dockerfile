FROM maven:3.5-jdk-8-onbuild-alpine
CMD ["java","-jar","/usr/src/app/target/event-math-interval-jar-with-dependencies.jar"]