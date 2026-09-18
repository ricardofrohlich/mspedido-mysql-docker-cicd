# etapa 1: build. usa uma imagem com jdk + maven só pra compilar e gerar o jar.
# essa etapa some no final (não vai pra imagem final), então o jdk completo
# não pesa na imagem que sobe.
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# copia primeiro só o que descreve as dependências (wrapper do maven + pom.xml).
# assim, se só o código mudar, o docker reaproveita o cache dessa camada e não
# baixa tudo de novo toda hora.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# só agora copia o código e compila
COPY src src
RUN ./mvnw -B clean package -DskipTests

# etapa 2: imagem final, só com o jre (não precisa de jdk/maven pra RODAR o jar)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
