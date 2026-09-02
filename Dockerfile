# Etapa 1: compila o projeto
FROM maven:3.9.11-eclipse-temurin-26 AS build

WORKDIR /app

# Copia primeiro o pom para aproveitar cache de dependências
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copia o código-fonte
COPY src ./src

# Gera o .jar
RUN mvn clean package -DskipTests -B


# Etapa 2: imagem final mais enxuta
FROM eclipse-temurin:26-jre

WORKDIR /app

# Copia o jar gerado na etapa de build
COPY --from=build /app/target/*.jar app.jar

# Railway injeta a porta pela variável PORT.
# Seu Spring já deve usar server.port=${PORT:8082}
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]