package cliente.udp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.CRC32; //Importación para el cálculo checsulm

public class ClienteEnviaUDP2 extends Thread {
    protected final int PUERTO_SERVER;
    protected final String SERVER;
    protected DatagramSocket socket; //socket de cliente
    protected InetAddress addressServer;
    private volatile boolean ejecutando = true;
    //Fila de espera para recibir los textos desde la Interfaz Gráfica
    private BlockingQueue<String> colaMensajes = new LinkedBlockingQueue<>();

    public ClienteEnviaUDP2(DatagramSocket nuevoSocket,String servidor,int puertoServidor) throws Exception {
        socket = nuevoSocket;
        SERVER = servidor;
        PUERTO_SERVER = puertoServidor;

        // resolver dirección
        //getByName hace consulta dns
        addressServer = InetAddress.getByName(SERVER);
    }

    //El método puente que llamará el botón de tu ventana gráfica
    public void encolarMensajeParaEnvio(String texto) {
        colaMensajes.add(texto);
    }

    @Override
    public void run() {
        try {
            EntradaSalida.mostrarMensaje("Cliente UDP listo y esperando mensajes de la interfaz...\n");

            while (ejecutando) {
                // En lugar de congelar el hilo esperando al teclado, 
                // take() duerme el hilo hasta que el usuario presione "Enviar" en la GUI
                String mensajeTexto = colaMensajes.take(); 

                // Invocamos el método de envío pasándole el texto directo
                Mensaje mensajeObj = enviaMensaje(mensajeTexto);

                // salir elegantemente
                if (mensajeObj.getMensaje().equalsIgnoreCase("fin")) {
                    EntradaSalida.mostrarMensaje("Finalizando envío UDP...\n");
                    ejecutando = false;
                }
            }
        }
        catch (InterruptedException e) {
            if (!ejecutando) return;
        }
        catch (Exception e) {
            System.err.println("Error cliente UDP envío: " + e.getMessage());
        }
        finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();  
                }
            }
            catch (Exception e) {
                System.err.println("Error cerrando recursos UDP: " + e.getMessage());
            }
        }
    }

    //Solo recibe el String en lugar del BufferedReader
    private Mensaje enviaMensaje(String mensaje) throws Exception {
        Mensaje mensajeObj = new Mensaje();

        // Cálculo del Checksum CRC32 
        byte[] textoBytes = mensaje.getBytes(StandardCharsets.UTF_8);
        CRC32 crc = new CRC32();
        crc.update(textoBytes, 0, textoBytes.length);
        long checksumGenerado = crc.getValue(); 

        // Empaquetado seguro en streams 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(mensaje);         
        dos.writeLong(checksumGenerado); 

        byte[] buffer = baos.toByteArray(); 

        // Creación y envío del datagrama 
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length, addressServer, PUERTO_SERVER);
        socket.send(paquete);

        //Llenado del objeto mensaje 
        mensajeObj.setMensaje(mensaje);
        mensajeObj.setChecksum(checksumGenerado);
        mensajeObj.setAddressServidor(paquete.getAddress());
        mensajeObj.setPuertoServidor(paquete.getPort());

        EntradaSalida.mostrarMensaje("Mensaje UDP \"" + mensajeObj.getMensaje()
                + "\" enviado con CRC32 [" + checksumGenerado + "] a servidor " 
                + mensajeObj.getAddressServidor() + ":" + mensajeObj.getPuertoServidor() + "\n");
                
        return mensajeObj;
    }

    public void detener() {
        ejecutando = false;
        this.interrupt(); // Despierta al hilo si estaba dormido en take()
        if (socket != null  && !socket.isClosed()) {
            socket.close();
        }
    }
}