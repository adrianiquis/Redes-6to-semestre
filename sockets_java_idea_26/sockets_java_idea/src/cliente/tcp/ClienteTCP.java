package cliente.tcp;

import java.io.File;

public  class ClienteTCP{
    protected final String SERVER;
    protected final int PUERTO_SERVER;
    private ClienteEnviaTCP2 clienteHilo;
    
    public ClienteTCP(String servidor,int puertoS){
        SERVER=servidor;
        PUERTO_SERVER=puertoS;
    }
    public void inicia()throws Exception{
        clienteHilo = new ClienteEnviaTCP2(SERVER,PUERTO_SERVER);
        clienteHilo.start(); //run
    }

    public void mandarArchivo(File archivo, javax.swing.JLabel label) {
        if (clienteHilo != null) {
            clienteHilo.encolarArchivoParaEnvio(archivo, label);
        }
    }
}
