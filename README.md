# 🌐 Proyecto de Redes: Sockets TCP y UDP con Interfaz Gráfica

**Universidad Autónoma de Zacatecas (UAZ)** **Ingeniería de Software - 6to Semestre** 

---

**Autores:** 
M. Josefina Adriana González Trejo y Pedro Abraham Ortega Hernandez

Este proyecto implementa una arquitectura de red cliente-servidor utilizando Sockets en Java. El sistema está dividido en dos partes principales: un backend (servidores TCP y UDP) que se ejecuta de forma aislada dentro de contenedores Docker, y un frontend (cliente GUI) que se ejecuta de forma nativa en la máquina host para facilitar la renderización de la interfaz.

---

## 🏗️ Arquitectura del Proyecto

Para mantener las mejores prácticas de desarrollo, el proyecto utiliza una arquitectura mixta:

* **Backend (Dockerizado):** Está compuesto por un **Servidor TCP** (maneja conexiones fiables orientadas a la conexión) y un **Servidor UDP** (maneja el envío de datagramas rápidos). Ambos servidores corren en su propia red de Docker (`bridge`) y exponen los puertos `5000` y `6000` respectivamente hacia el exterior.
* **Frontend (Ejecución Local):** Se trata del **Cliente GUI**, una interfaz gráfica desarrollada en Java que se ejecuta de manera local desde el IDE (fuera de Docker) conectándose a los servidores a través de `localhost`.

---

## 📂 Estructura de Carpetas Principal

    SOCKETS_JAVA_IDEA/
    ├── Dockerfile                 # Compila y configura la imagen de los servidores
    ├── docker-compose.yml         # Orquesta los contenedores TCP y UDP
    └── src/
        ├── cliente/               # Clases de conexión del cliente
        ├── servidor/
        │   ├── tcp/               # Lógica y manejadores del servidor TCP
        │   └── udp/               # Lógica del servidor UDP
        └── vista/                 # Interfaz gráfica (InterfazChat.java)

---

## ⚙️ Requisitos Previos

* **Java JDK** instalado en tu máquina (se recomienda JDK 17 o superior) para correr el cliente.
* **IntelliJ IDEA** (o cualquier IDE de Java compatible).
* **Docker** y **Docker Compose** instalados y en ejecución en tu sistema.

---

## 🚀 Instrucciones de Ejecución

La ejecución consta de dos fases: levantar los servidores (backend) y ejecutar la interfaz gráfica (frontend).

### Fase 1: Iniciar los Servidores (Backend)

1. Abre una terminal y navega hasta la carpeta raíz del proyecto (donde se encuentra el archivo `docker-compose.yml`).
2. Ejecuta el siguiente comando para compilar las clases y levantar ambos servidores en segundo plano:

    docker-compose up --build -d

> **Nota:** Para verificar que los servidores están corriendo correctamente y observar el registro de mensajes, puedes usar el comando: `docker-compose logs -f`

### Fase 2: Iniciar la Interfaz de Usuario (Frontend)

1. Abre el proyecto en tu entorno de desarrollo (por ejemplo, IntelliJ IDEA).
2. Asegúrate de que en el código de conexión del cliente (`ClienteTCP.java`, `ClienteEnviaTCP2.java`, etc.), la dirección IP objetivo sea **`localhost`** o **`127.0.0.1`**, ya que Docker está exponiendo los puertos hacia tu máquina local.
3. Navega hasta el archivo `src/vista/InterfazChat.java`.
4. Ejecuta la clase principal (`Run 'InterfazChat.main()'`).
5. La ventana de la interfaz gráfica se abrirá de forma nativa en tu computadora y ya podrás interactuar con los servidores que están aislados dentro de Docker.

---

## 🛑 Detener el Proyecto

Para apagar los servidores y limpiar la red creada por Docker, ejecuta el siguiente comando en tu terminal desde la raíz del proyecto:

    docker-compose down
