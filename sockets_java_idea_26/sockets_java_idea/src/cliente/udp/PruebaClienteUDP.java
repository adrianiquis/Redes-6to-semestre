package cliente.udp;

public class PruebaClienteUDP{
    public static void main(String args[]) throws Exception{
        ClienteUDP clienteUDP =new ClienteUDP("10.10.28.30",50000);
        
        clienteUDP.inicia();
    }
}
