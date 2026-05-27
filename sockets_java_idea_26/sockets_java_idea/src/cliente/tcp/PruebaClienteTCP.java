package cliente.tcp;

import java.io.File;
import java.io.FileWriter;

public class PruebaClienteTCP{
    public static void main(String args[])throws Exception{
        //puertoS -> el el puerto del servidor que recibe
        //En servidor correr el servidor con nuestra ip en 'servidor'
        ClienteTCP clienteTCP =new ClienteTCP("127.0.0.1",60000);
             
        clienteTCP.inicia();

        // 2. --- CÓDIGO TEMPORAL PARA PROBAR EL ENVÍO ---
        // Creamos un archivo de texto rapidísimo por código
        /*File archivoPrueba = new File("prueba_redes.txt");
        FileWriter writer = new FileWriter(archivoPrueba);
        writer.write("¡Hola! El requerimiento de archivos pesados, métricas y TCP funciona al 100%.");
        writer.close();

        System.out.println("Archivo de prueba generado localmente. Enviando al servidor...\n");

        // 3. Usamos el método puente que acabamos de crear para mandar el archivo
        clienteTCP.mandarArchivo(archivoPrueba, null);*/
    }
}
