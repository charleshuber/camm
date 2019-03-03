FROM maven:3.3-jdk-8-alpine

# add glibc dependency for opencv shared librairy
COPY glibc-2.29-r0.apk ./

run apk update

RUN apk --allow-untrusted --force add glibc-2.29-r0.apk

RUN mkdir -p src/main

RUN mkdir static

COPY src/main src/main/

COPY static static/

COPY pom.xml ./

RUN keytool -genkeypair -alias camm -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src/main/resources/keystore/camm.p12 -validity 8000 -storepass cammpass -dname "CN=camm, OU=, O=, L=, S=, C=" 

RUN mvn clean install

EXPOSE 8081

EXPOSE 8090

CMD ["java", "-jar", "target/camm-1.0.0.jar"]
