package servidor.tcp;

public class PruebaServidorTCP{
    public static void main(String args[])throws Exception{
        //Va a escuchar conexiones en el puerto 6000
        ServidorTCP servidorTCP=new ServidorTCP(60000);
        
        servidorTCP.inicia();
    }
}
