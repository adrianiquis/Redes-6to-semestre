# Usamos una imagen de Java con el JDK para poder compilar el código
FROM eclipse-temurin:17-jdk

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos la carpeta de código fuente y la de descargas al contenedor
COPY src/ ./src/
COPY descargas_servidor/ ./descargas_servidor/

# Creamos una carpeta 'out' y compilamos todos los archivos .java respetando los paquetes
RUN mkdir out && javac -d out $(find src -name "*.java")

# Nos movemos a la carpeta de compilados para que Java encuentre los paquetes correctamente
WORKDIR /app/out