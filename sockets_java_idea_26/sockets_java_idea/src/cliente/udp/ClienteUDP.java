package cliente.udp;

import java.net.*;
//import java.io.*;
 
//declaramos la clase udp
public class ClienteUDP{
    protected final int PUERTO_SERVER;
    protected final String SERVER;
    
    public ClienteUDP(String servidor, int puertoS){
        PUERTO_SERVER=puertoS;
        SERVER=servidor;
    }
    
    public void inicia()throws Exception{
        DatagramSocket socket=new DatagramSocket(); //UDP
        
        ClienteEscuchaUDP2 clienteEnvUDP=new ClienteEscuchaUDP2(socket);
        ClienteEnviaUDP2 clienteEscUDP=new ClienteEnviaUDP2(socket, SERVER, PUERTO_SERVER);
        
        clienteEnvUDP.start(); //método run del hilo
        clienteEscUDP.start();
    }
}
