package servidor.tcp;

import datos.EntradaSalida;
import java.net.*;
import java.io.*;

public class ManejadorCliente implements Runnable {
    private Socket socket_cli;
    private String carpetaDescargas;

    // El constructor recibe el socket del cliente y dónde guardar las cosas
    public ManejadorCliente(Socket socket_cli, String carpetaDescargas) {
        this.socket_cli = socket_cli;
        this.carpetaDescargas = carpetaDescargas;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(socket_cli.getInputStream())) {
            
            // Bucle infinito para recibir múltiples archivos en la misma conexión persistente
            while (true) {
                try {
                    // Leer metadatos del archivo
                    String nombreArchivo = in.readUTF();
                    long tamanoArchivo = in.readLong();

                    EntradaSalida.mostrarMensaje("\nRecibiendo archivo: " + nombreArchivo + " (" + tamanoArchivo + " bytes)...\n");

                    // Preparar el archivo de salida
                    File archivoDestino = new File(carpetaDescargas + nombreArchivo);
                    if (archivoDestino.exists()) {
                        nombreArchivo = System.currentTimeMillis() + "_" + nombreArchivo;
                        archivoDestino = new File(carpetaDescargas + nombreArchivo);
                    }

                    // Recibir los bytes y guardarlos en el disco
                    try (FileOutputStream fos = new FileOutputStream(archivoDestino)) {
                        byte[] buffer = new byte[8192]; 
                        long bytesRecibidos = 0;
                        int bytesLeidos;

                        while (bytesRecibidos < tamanoArchivo) {
                            int bytesPendientes = (int) Math.min(buffer.length, tamanoArchivo - bytesRecibidos);
                            
                            bytesLeidos = in.read(buffer, 0, bytesPendientes);
                            if (bytesLeidos == -1) {
                                throw new EOFException("El cliente cerró la conexión inesperadamente.");
                            }
                            
                            fos.write(buffer, 0, bytesLeidos);
                            bytesRecibidos += bytesLeidos;
                        }
                        fos.flush(); 
                    }

                    EntradaSalida.mostrarMensaje("Archivo guardado exitosamente en: " + archivoDestino.getAbsolutePath() + "\n");
                    EntradaSalida.mostrarMensaje("Esperando nuevo archivo del cliente...\n");

                } catch (EOFException e) {
                    EntradaSalida.mostrarMensaje("El cliente TCP finalizó la conexión persistente.\n");
                    break; // Salimos del bucle si el cliente se desconecta
                } catch (SocketException e) {
                     EntradaSalida.mostrarMensaje("Conexión perdida con el cliente TCP.\n");
                     break;
                }
            }
        } catch (Exception e) {
            EntradaSalida.mostrarMensaje("Error procesando cliente TCP: " + e.getMessage() + "\n");
        } finally {
            try {
                if (socket_cli != null && !socket_cli.isClosed()) {
                    socket_cli.close();
                }
            } catch (IOException e) {
                System.err.println("Error cerrando socket de cliente: " + e.getMessage());
            }
        }
    }
}