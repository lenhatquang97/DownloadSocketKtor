FROM openjdk:11
COPY . .
RUN ./gradlew :buildFatJar
EXPOSE 23567
CMD ["java", "-jar", "build/libs/fat.jar"]