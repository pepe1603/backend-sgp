# ----------------------------------------------------
# ETAPA 1: BUILD (Compila el código y genera el JAR)
# ----------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia los archivos de configuración de Maven (pom.xml) y descarga dependencias
# Esto permite que Docker cachee las dependencias si el pom.xml no cambia
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia el código fuente
COPY src/ ./src/

# Empaqueta la aplicación Spring Boot en un JAR
RUN mvn package -DskipTests

# ----------------------------------------------------
# ETAPA 2: RUNTIME (Ejecuta la aplicación, imagen más ligera)
# ----------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

# Argumento para obtener el nombre del JAR (definido en el pom.xml)
ARG JAR_FILE=target/sgp-backend-0.0.1-SNAPSHOT.jar

# Copia el JAR generado en la etapa BUILD
COPY --from=builder /app/${JAR_FILE} app.jar

# Expone el puerto de la aplicación (definido en docker-compose)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app.jar"]