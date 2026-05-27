package servidor.udp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32; // Importación para el checksum

public class ServidorEscuchaUDP2 extends Thread {
    protected DatagramSocket socket; //socket UDP userdatagramprotocol6
    protected final int PUERTO_SERVER;

    // Tamaño de buffer razonable
    private static final int MAX_BUFFER = 1024;

    public ServidorEscuchaUDP2(int puertoS)throws Exception {
        PUERTO_SERVER = puertoS;

        // Crear socket UDP
        socket = new DatagramSocket(PUERTO_SERVER);
        // Timeout opcional
        // socket.setSoTimeout(5000);
    }

    @Override
    public void run() {
        try {
            EntradaSalida.mostrarMensaje("Servidor UDP escuchando en puerto "+ PUERTO_SERVER + "...\n");

            // El servidor UDP normalmente nunca termina
            while (true) {
                try {
                    // recibir datagrama
                    Mensaje mensajeObj = recibeMensaje();

                    // procesar lógica
                    //procesaMensaje(mensajeObj);

                    // procesar lógica solo si el mensaje no está corrupto
                    if (!mensajeObj.getMensaje().equals("ERROR_CORRUPCION")) {
                        procesaMensaje(mensajeObj);
                    }
                }
                catch (SocketTimeoutException e) {
                    EntradaSalida.mostrarMensaje("Timeout esperando paquetes...\n");
                }
                catch (Exception e) {
                    EntradaSalida.mostrarMensaje( "Error procesando paquete: "+ e.getMessage() + "\n");
                }
            }

        }
        catch (Exception e) {
            System.err.println("Error servidor UDP: "+ e.getMessage());
        }
        finally {
            if (socket != null && !socket.isClosed()) { //cerrar socket
                socket.close();
            }
        }
    }

    private void procesaMensaje(Mensaje mensajeObj) throws Exception {

        String mensaje = mensajeObj.getMensaje();
        String respuesta;

        // Comparaciones de msg
        if (mensaje.equalsIgnoreCase("hola")) {
            respuesta = "¿Como estas?";
        }
        else if (mensaje.equalsIgnoreCase("bien y tu?")) {
            respuesta = "Estoy pal arrastre, aunque gracias por preguntar";
        }
        else if (mensaje.equalsIgnoreCase("fin")) {
            // NO cerrar servidor
            respuesta = "Cliente finalizo comunicacion";
        }
        else {
            respuesta = "Servidor no entiende: " + mensaje;
        }

        mensajeObj.setMensaje(respuesta);
        enviaMensaje(mensajeObj);
    }

    private Mensaje recibeMensaje() throws Exception {

        Mensaje mensajeObj = new Mensaje();
        // buffer recepción
        byte[] buffer = new byte[MAX_BUFFER];
        //Datagrama
        DatagramPacket paquete = new DatagramPacket( buffer, buffer.length);

        // Se queda bloqueante en espera
        socket.receive(paquete);

        // convertir bytes a cadena String
        //String mensaje =  new String(paquete.getData(), 0, paquete.getLength(), StandardCharsets.UTF_8);
        
        // Desempaquetar los datos usando streams
        ByteArrayInputStream bais = new ByteArrayInputStream(paquete.getData(), 0, paquete.getLength());
        DataInputStream dis = new DataInputStream(bais);

        String mensajeRecibido = dis.readUTF(); // Leemos primero el texto
        long checksumRecibido = dis.readLong(); // Luego leemos los 8 bytes del código

        // Recalcular el Checksum con el texto recibido
        byte[] textoBytes = mensajeRecibido.getBytes(StandardCharsets.UTF_8);
        CRC32 crc = new CRC32();
        crc.update(textoBytes, 0, textoBytes.length);
        long checksumCalculado = crc.getValue();

        // Detectar errores 
        if (checksumRecibido != checksumCalculado) {
            EntradaSalida.mostrarMensaje("¡ALERTA! Paquete corrupto. Descartando...\n");
            mensajeObj.setMensaje("ERROR_CORRUPCION"); // Bandera para no procesarlo
        } else {
            mensajeObj.setMensaje(mensajeRecibido);
            EntradaSalida.mostrarMensaje("Mensaje íntegro recibido: \"" + mensajeRecibido + "\" (CRC OK)\n");
        }

        //mensajeObj.setMensaje(mensaje);
        // datos cliente
        mensajeObj.setChecksum(checksumRecibido);
        mensajeObj.setAddressCliente(paquete.getAddress());
        mensajeObj.setPuertoCliente(paquete.getPort());

        EntradaSalida.mostrarMensaje("Mensaje recibido \"" + mensajeObj.getMensaje() + "\" del cliente "
                + mensajeObj.getAddressCliente() + ":" + mensajeObj.getPuertoCliente()+ "\n");

        return mensajeObj;
    }

    private void enviaMensaje( Mensaje mensajeObj) throws Exception {

        // String a  bytes UTF-8
        //byte[] buffer =  mensajeObj.getMensaje().getBytes(StandardCharsets.UTF_8);

        // Preparar la respuesta y calcular su Checksum
        String respuesta = mensajeObj.getMensaje();
        byte[] textoBytes = respuesta.getBytes(StandardCharsets.UTF_8);

        CRC32 crc = new CRC32();
        crc.update(textoBytes, 0, textoBytes.length);
        long checksumGenerado = crc.getValue();

        // Empaquetar Texto + Checksum
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(respuesta);
        dos.writeLong(checksumGenerado);
        
        byte[] buffer = baos.toByteArray();

        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length, mensajeObj.getAddressCliente(), mensajeObj.getPuertoCliente());

        // envío UDP
        socket.send(paquete);

        EntradaSalida.mostrarMensaje( "Mensaje enviado \"" + mensajeObj.getMensaje() + "\" al cliente "
                + mensajeObj.getAddressCliente() + ":" + mensajeObj.getPuertoCliente() + "\n");
    }
}