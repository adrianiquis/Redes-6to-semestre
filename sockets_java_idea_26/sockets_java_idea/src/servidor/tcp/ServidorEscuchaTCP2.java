package servidor.tcp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
import java.io.*;
import java.util.function.Consumer;

//Thread es un progrma en hilo, puede ejecutarse sin necesidad de otros
public class ServidorEscuchaTCP2 extends Thread {
    protected ServerSocket socket; //Socket servidor
    protected final int PUERTO_SERVER;
    private volatile boolean ejecutando = true;
    private final Consumer<File> onArchivoRecibido;
    // Directorio donde el servidor guardará los archivos recibidos
    private final String CARPETA_DESCARGAS = "descargas_servidor/";

    public ServidorEscuchaTCP2(int puertoS, Consumer<File> onArchivoRecibido) throws Exception {
        PUERTO_SERVER = puertoS;
        this.onArchivoRecibido = onArchivoRecibido;
        // Primitiva de LISTEN, crea socket con Ip (implìcita activa) y puerto
        socket = new ServerSocket(PUERTO_SERVER);

        // Crear carpeta de descargas si no existe
        File directorio = new File(CARPETA_DESCARGAS);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
    }

    @Override
    public void run() {
        try {
            EntradaSalida.mostrarMensaje("Servidor TCP escuchando en puerto " + PUERTO_SERVER + "...\n");

            // El servidor queda esperando clientes siempre
            while (ejecutando) {
                try{
                    // Primitiva ACCEPT, acepta conexiones de clientes
                    Socket socket_cli = socket.accept();
                    EntradaSalida.mostrarMensaje("Cliente TCP conectado desde"+ socket_cli.getInetAddress()+ ":" + socket_cli.getPort() + "\n");

                    // En lugar de manejar al cliente aquí y bloquear el servidor,
                    // lanzamos un nuevo hilo por cada cliente conectado. 
                    // Así el servidor puede atender a múltiples clientes a la vez.
                    // Invoca a la nueva clase externa y le pasa el socket y la ruta
                    ManejadorCliente manejador = new ManejadorCliente(socket_cli, CARPETA_DESCARGAS, onArchivoRecibido);
                    new Thread(manejador).start();

                }catch(SocketException e){
                    if(!ejecutando){
                        EntradaSalida.mostrarMensaje("Servidor TCP detenido.\n");
                        break;
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.println( "Error en servidor: " + e.getMessage());
        } finally {
            detener();
        }
    }

    public void detener() {
        ejecutando = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error cerrando el socket del servidor: " + e.getMessage());
        }
    }
}