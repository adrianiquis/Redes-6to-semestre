package servidor.tcp;

import java.io.File;
import java.util.function.Consumer;

public class ServidorTCP{
    protected final int PUERTO_SERVER;

    public ServidorTCP(int puertoS){
        PUERTO_SERVER=puertoS;
    }

    public void inicia() throws Exception {
        inicia(null);
    }

    public void inicia(Consumer<File> onArchivoRecibido) throws Exception {
        ServidorEscuchaTCP2 servidorTCP = new ServidorEscuchaTCP2(PUERTO_SERVER, onArchivoRecibido);
        servidorTCP.start();
    }
}
