FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar apenas arquivos necessários para download de dependências
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Dar permissão de execução para o maven wrapper
RUN chmod +x ./mvnw

# Download de dependências (fica em cache se pom.xml não mudar)
RUN ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime stage (imagem final)
FROM eclipse-temurin:21-jre-alpine

# Instalar curl para health checks
RUN apk add --no-cache curl

# Criar usuário não-root para segurança
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Criar diretório da aplicação
WORKDIR /app

# Copiar JAR do stage de build
COPY --from=builder /app/target/*.jar app.jar

# Mudar ownership para usuário não-root
RUN chown appuser:appgroup app.jar

# Trocar para usuário não-root
USER appuser

# Expor porta (Spring Boot padrão)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM otimizada para containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Comando para executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]