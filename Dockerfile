# Usa a imagem do Java 21 (ou a versão compatível com a compilação do seu projeto)
FROM eclipse-temurin:26-jdk-alpine

WORKDIR /app

# Copia o .jar gerado na pasta target para dentro do container
COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]