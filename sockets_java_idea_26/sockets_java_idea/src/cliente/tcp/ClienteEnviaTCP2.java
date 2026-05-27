package cliente.tcp;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClienteEnviaTCP2 extends Thread {

    private final String host;
    private final int puerto;
    
    // Cola para almacenar las peticiones de envío (Archivo + Etiqueta visual)
    private final BlockingQueue<PeticionEnvio> colaEnvio = new LinkedBlockingQueue<>();

    // Clase interna para empaquetar el archivo con su label
    private static class PeticionEnvio {
        File archivo;
        JLabel labelMetricas;
        PeticionEnvio(File archivo, JLabel labelMetricas) {
            this.archivo = archivo;
            this.labelMetricas = labelMetricas;
        }
    }

    public ClienteEnviaTCP2(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    // Método que llama la interfaz gráfica
    public void encolarArchivoParaEnvio(File archivo, JLabel labelMetricas) {
        colaEnvio.offer(new PeticionEnvio(archivo, labelMetricas));
    }

    @Override
    public void run() {
        while (true) {
            try {
                // El hilo se queda dormido aquí hasta que la interfaz le mande un archivo
                PeticionEnvio peticion = colaEnvio.take();
                enviarArchivoRed(peticion.archivo, peticion.labelMetricas);
            } catch (InterruptedException e) {
                System.err.println("Hilo TCP interrumpido.");
                break;
            }
        }
    }

    private void enviarArchivoRed(File archivo, JLabel label) {
        try (Socket socket = new Socket(host, puerto);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(archivo)) {

            long tamañoTotal = archivo.length();
            
            // 1. Mandamos metadatos básicos (nombre y tamaño) al servidor
            dos.writeUTF(archivo.getName());
            dos.writeLong(tamañoTotal);

            byte[] buffer = new byte[8192]; // Pedazos de 8KB
            int bytesLeidos;
            long bytesEnviadosSoFar = 0;

            // --- INICIA EL CRONÓMETRO ---
            long startTime = System.currentTimeMillis();

            // 2. Bucle de envío de archivo en fragmentos
            while ((bytesLeidos = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesLeidos);
                dos.flush();
                
                bytesEnviadosSoFar += bytesLeidos;
                long currentTime = System.currentTimeMillis();
                long transcurridoMs = currentTime - startTime;

                // Evitar división por cero en los primeros milisegundos
                if (transcurridoMs > 0 && label != null) {
                    
                    // CÁLCULO 1: Tiempo Transcurrido (en segundos)
                    double transcurridoSeg = transcurridoMs / 1000.0;
                    
                    // CÁLCULO 2: Tasa de Transferencia (bps)
                    // (Bytes * 8 = bits) / segundos
                    double bps = (bytesEnviadosSoFar * 8.0) / transcurridoSeg;
                    
                    // CÁLCULO 3: Tiempo Restante
                    double bytesPorSegundo = bytesEnviadosSoFar / transcurridoSeg;
                    long bytesQueFaltan = tamañoTotal - bytesEnviadosSoFar;
                    double restanteSeg = (bytesPorSegundo > 0) ? (bytesQueFaltan / bytesPorSegundo) : 0;
                    
                    // Porcentaje para mostrar algo bonito
                    int porcentaje = (int) ((bytesEnviadosSoFar * 100) / tamañoTotal);

                    // Construimos el texto con formato
                    String textoMetricas = String.format(
                        " %d%% | 🚄 BPS: %.0f bps | ⏱ Transcurrido: %.1fs | ⏳ Restante: %.1fs",
                        porcentaje, bps, transcurridoSeg, restanteSeg
                    );

                    // Actualizamos la ventana (Usamos invokeLater para no trabar los gráficos)
                    SwingUtilities.invokeLater(() -> label.setText(textoMetricas));
                }
            }

            // --- TERMINA EL CRONÓMETRO ---
            long endTime = System.currentTimeMillis();
            
            // CÁLCULO 4: Latencia / Tiempo Total de Transmisión
            double latenciaTotal = (endTime - startTime) / 1000.0;
            
            if (label != null) {
                SwingUtilities.invokeLater(() -> {
                    label.setText(String.format(" ¡Éxito! Latencia/Tiempo Total: %.2f segundos", latenciaTotal));
                    label.setForeground(new java.awt.Color(0, 150, 0)); // Pintarlo de verde
                });
            }

            System.out.println("Archivo enviado completamente por TCP.");

        } catch (Exception e) {
            System.err.println("Error enviando archivo: " + e.getMessage());
            if (label != null) {
                SwingUtilities.invokeLater(() -> {
                    label.setText(" Error al enviar el archivo.");
                    label.setForeground(java.awt.Color.RED);
                });
            }
        }
    }
}