package cliente.udp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClienteEscuchaUDP2 extends Thread {
    protected final int PUERTO_CLIENTE;
    protected DatagramSocket socket; //socket del cliente
    private static final int MAX_BUFFER = 1024;
    private volatile boolean ejecutando = true;//Forma de dos hilos, que comparten una variable
    

    public ClienteEscuchaUDP2(DatagramSocket socketNuevo) {
        socket = socketNuevo;
        PUERTO_CLIENTE =socket.getLocalPort();
    }

    @Override
    public void run() {
        try {
            EntradaSalida.mostrarMensaje( "Cliente UDP escuchando en puerto "+ PUERTO_CLIENTE + "\n");

            while (ejecutando) {
                try {
                    Mensaje mensajeObj = recibeMensaje();

                    String mensaje = mensajeObj.getMensaje();

                    // protocolo simple
                    if (mensaje.equalsIgnoreCase("fin")) {
                        EntradaSalida.mostrarMensaje("Servidor finalizó comunicación\n");
                        ejecutando = false;
                    }
                }
                catch (SocketTimeoutException e) {
                    EntradaSalida.mostrarMensaje("Esperando mensajes UDP...\n");
                }
                catch (SocketException e) { //cuando el socket está cerrado marca esta excepción
                    if (socket.isClosed()) {
                        EntradaSalida.mostrarMensaje("Socket UDP cerrado\n");
                    }
                    else {
                        EntradaSalida.mostrarMensaje("Error de socket: "+ e.getMessage() + "\n");
                    }
                    ejecutando = false;
                }
                catch (Exception e) {
                    EntradaSalida.mostrarMensaje(ejecutando +" Error recibiendo mensaje: "+ e.getMessage() + "\n");
                }
            }
        }
        catch (Exception e) {
            System.err.println("Error cliente UDP: "+ e.getMessage());
        }
        finally {
            if (socket != null && !socket.isClosed()) {
                socket.close(); //cerrar socket
            }
            EntradaSalida.mostrarMensaje( "Cliente UDP finalizado\n");
        }
    }

    private Mensaje recibeMensaje() throws Exception {

        Mensaje mensajeObj = new Mensaje();

        // buffer recepción
        byte[] buffer = new byte[MAX_BUFFER];

        DatagramPacket paquete = new DatagramPacket(buffer,buffer.length);

        // Se queda bloqueante recibiendo
        socket.receive(paquete);

        // bytes a String correctamente
        String mensaje = new String(paquete.getData(),0, paquete.getLength(), StandardCharsets.UTF_8);
        mensajeObj.setMensaje(mensaje);
        mensajeObj.setAddressServidor(paquete.getAddress());
        mensajeObj.setPuertoServidor(paquete.getPort());

        EntradaSalida.mostrarMensaje("Mensaje recibido \"" + mensajeObj.getMensaje()
                + "\" de servidor " + mensajeObj.getAddressServidor()+ ":"
                + mensajeObj.getPuertoServidor() + "\n");

        return mensajeObj;
    }

    // detener hilo manualmente
    public void detener() {
        ejecutando = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}