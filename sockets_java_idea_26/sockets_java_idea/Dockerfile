# Usamos la imagen de Java
FROM eclipse-temurin:17-jdk

# INSTALAMOS LAS LIBRERÍAS GRÁFICAS DE X11 PARA JAVA SWING
RUN apt-get update && apt-get install -y \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libx11-6 \
    && rm -rf /var/lib/apt/lists/*

# Directorio de trabajo
WORKDIR /app

# Copiamos las carpetas
COPY src/ ./src/
COPY descargas_servidor/ ./descargas_servidor/

# Compilamos
RUN mkdir out && javac -d out $(find src -name "*.java")

# Nos movemos a la carpeta compilada
WORKDIR /app/out