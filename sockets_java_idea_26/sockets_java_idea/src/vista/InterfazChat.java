package vista;

import cliente.tcp.ClienteTCP;
import cliente.udp.ClienteEnviaUDP2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class InterfazChat extends JFrame {

    // Componentes Visuales
    private JTextPane panelChat;
    private JTextField campoMensaje, campoIPDestino;
    private JButton btnEnviarTexto, btnEnviarArchivo, btnConectar;
    private JLabel labelMetricas, labelEstadoRed;

    // Motores de Red (El mismo motor)
    private ClienteTCP clienteTcp;
    private ClienteEnviaUDP2 clienteUdp;

    // Puertos P2P (teléfono de vasos)
    private final int PUERTO_TCP_AMIGO = 60000; 
    private final int PUERTO_UDP_AMIGO = 50000; 
    private final int PUERTO_MI_ESCUCHA_UDP = 50000; 

    //Paleta de colores
    private final Color COLOR_FONDO_VENTANA = new Color(240, 248, 255); // Alice Blue (Blanco azulado suave)
    private final Color COLOR_PANEL_CHAT = Color.WHITE; // El chat se mantiene blanco puro
    private final Color COLOR_PRIMARIO_PASTEL = new Color(174, 217, 224); // Azul cielo pastel para botones
    private final Color COLOR_BURBUJA_USUARIO = new Color(209, 255, 209); // Verde menta pastel muy suave
    private final Color COLOR_BURBUJA_AMIGO = new Color(255, 209, 220); // Gris perla pastel suave (o rosa empolvado)
    private final Color COLOR_TEXTO_OSCURO = new Color(60, 60, 60); // Gris oscuro suave para textos legibles
    private final Font FUENTE_MODERNA = new Font("Segoe UI", Font.PLAIN, 14);

    public InterfazChat() {
        //Look y Feel Moderno (Nativo)
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        
        setTitle("Chat Redes P2P - Pastel Edition");
        setSize(650, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_FONDO_VENTANA); // Fondo suave a la ventana
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // Panel superior: Configuración IP Dinámica
        JPanel panelNorte = new JPanel(new BorderLayout(10, 0));
        panelNorte.setBackground(COLOR_FONDO_VENTANA);
        panelNorte.setBorder(new EmptyBorder(15, 15, 0, 15)); 

        JPanel panelIP = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelIP.setBackground(COLOR_FONDO_VENTANA);
        JLabel lblIP = new JLabel("IP del Destinatario:");
        lblIP.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIP.setForeground(COLOR_TEXTO_OSCURO);
        
        campoIPDestino = new JTextField("127.0.0.1", 15);
        campoIPDestino.setFont(FUENTE_MODERNA);
        campoIPDestino.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        
        btnConectar = crearBotónEstilizado("Establecer Red", COLOR_PRIMARIO_PASTEL);
        labelEstadoRed = new JLabel("● Desconectado");
        labelEstadoRed.setForeground(Color.RED);

        panelIP.add(lblIP);
        panelIP.add(campoIPDestino);
        panelIP.add(btnConectar);
        panelIP.add(labelEstadoRed);
        panelNorte.add(panelIP, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // Panel central: Burbujas de Chat (JTextPane estilizado)
        panelChat = new JTextPane();
        panelChat.setEditable(false);
        panelChat.setBackground(COLOR_PANEL_CHAT);
        // Definir estilos de burbujas pasteles
        configurarEstilosChat();
        
        JScrollPane scrollChat = new JScrollPane(panelChat);
        scrollChat.setBorder(new EmptyBorder(10, 15, 10, 15)); 
        scrollChat.getViewport().setBackground(COLOR_PANEL_CHAT);
        add(scrollChat, BorderLayout.CENTER);

        // Panel inferior: Entrada de Texto y Botones
        JPanel panelSur = new JPanel(new BorderLayout(10, 10));
        panelSur.setBackground(COLOR_FONDO_VENTANA);
        panelSur.setBorder(new EmptyBorder(0, 15, 15, 15));

        // Fila 1: Texto
        JPanel panelEntrada = new JPanel(new BorderLayout(5, 0));
        panelEntrada.setBackground(COLOR_FONDO_VENTANA);
        campoMensaje = new JTextField();
        campoMensaje.setFont(FUENTE_MODERNA);
        campoMensaje.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        
        btnEnviarTexto = crearBotónEstilizado("Enviar (UDP)", COLOR_PRIMARIO_PASTEL);
        btnEnviarTexto.setEnabled(false); // Deshabilitado hasta conectar

        panelEntrada.add(campoMensaje, BorderLayout.CENTER);
        panelEntrada.add(btnEnviarTexto, BorderLayout.EAST);

        // Archivos y Métricas
        JPanel panelAccionesExtra = new JPanel(new BorderLayout(10, 0));
        panelAccionesExtra.setBackground(COLOR_FONDO_VENTANA);
        // Usamos un color pastel diferente para el clip
        btnEnviarArchivo = crearBotónEstilizado("📎 Adjuntar Archivo/Foto (TCP)", new Color(255, 228, 225)); // Misty Rose pastel
        btnEnviarArchivo.setEnabled(false); // Deshabilitado hasta conectar
        
        labelMetricas = new JLabel(" Métricas TCP: Esperando transferencia...");
        labelMetricas.setFont(new Font("Consolas", Font.BOLD, 12));
        labelMetricas.setForeground(new Color(100, 100, 100)); // Gris medio pastel

        panelAccionesExtra.add(btnEnviarArchivo, BorderLayout.WEST);
        panelAccionesExtra.add(labelMetricas, BorderLayout.CENTER);

        panelSur.add(panelEntrada, BorderLayout.NORTH);
        panelSur.add(panelAccionesExtra, BorderLayout.SOUTH);
        add(panelSur, BorderLayout.SOUTH);

        // GESTIÓN DE EVENTOS
        btnConectar.addActionListener(e -> inicializarRedDinamica());
        btnEnviarTexto.addActionListener(e -> enviarTexto());
        campoMensaje.addActionListener(e -> enviarTexto());
        btnEnviarArchivo.addActionListener(e -> seleccionarYEnviarArchivo());
    }

    
    // Cambia el diseño de los botones para que se vean pasteles, modernos y redondeados
    private JButton crearBotónEstilizado(String texto, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(colorFondo);
        
        // Letras en gris oscuro suave para legibilidad sobre pastel
        boton.setForeground(COLOR_TEXTO_OSCURO); 
        
        boton.setFocusPainted(false);
        // Bordes suaves y redondeados
        boton.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(8, 20, 8, 20)
        ));
        return boton;
    }

    // Configura los estilos (burbujas) pasteles para el JTextPane
    private void configurarEstilosChat() {
        StyledDocument doc = panelChat.getStyledDocument();
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        // Estilo para quien manda (Verde menta pastel, derecha, bold)
        Style estiloTu = doc.addStyle("estiloTu", def);
        StyleConstants.setForeground(estiloTu, COLOR_TEXTO_OSCURO);
        StyleConstants.setBackground(estiloTu, COLOR_BURBUJA_USUARIO);
        StyleConstants.setBold(estiloTu, true);
        StyleConstants.setFontFamily(estiloTu, "Segoe UI Semibold");

        // Estilo para quien recibe (Gris perla pastel, izquierda)
        Style estiloAmigo = doc.addStyle("estiloAmigo", def);
        StyleConstants.setForeground(estiloAmigo, Color.BLACK);
        StyleConstants.setBackground(estiloAmigo, COLOR_BURBUJA_AMIGO);
    }

    // Inicializa los motores apuntando a la IP que tecleaste
    private void inicializarRedDinamica() {
        String ipTarget = campoIPDestino.getText().trim();
        if (ipTarget.isEmpty() || ipTarget.split("\\.").length != 4) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa una IP IPv4 válida (ej. 10.10.28.145).", "IP Incorrecta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            labelEstadoRed.setText("● Conectando...");
            labelEstadoRed.setForeground(Color.ORANGE);

            if (clienteUdp != null) clienteUdp.detener();

            // Motor TCP
            clienteTcp = new ClienteTCP(ipTarget, PUERTO_TCP_AMIGO);
            clienteTcp.inicia();
            
            // Motor UDP
            clienteUdp = new ClienteEnviaUDP2(new DatagramSocket(), ipTarget, PUERTO_UDP_AMIGO);
            clienteUdp.start(); 
            
            //El oído P2P (Receptor UDP integrado)
            iniciarEscuchaRespuestasUDP();

            // Actualizar UI pastel
            labelEstadoRed.setText("● Conectado a " + ipTarget);
            labelEstadoRed.setForeground(new Color(0, 180, 0)); // Un verde más brillante pastel
            campoIPDestino.setEnabled(false);
            btnConectar.setEnabled(false);
            btnEnviarTexto.setEnabled(true);
            btnEnviarArchivo.setEnabled(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error de red: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            labelEstadoRed.setText("● Error");
            labelEstadoRed.setForeground(Color.RED);
        }
    }

    private void enviarTexto() {
        String texto = campoMensaje.getText().trim();
        if (!texto.isEmpty() && clienteUdp != null) {
            agregarTextoAlChat("Tú: " + texto + "\n", "estiloTu");
            clienteUdp.encolarMensajeParaEnvio(texto);
            campoMensaje.setText(""); 
        }
    }

    // ¡AQUÍ ESTÁ LA CORRECCIÓN! Conectamos el botón para abrir el archivo
    private void seleccionarYEnviarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        int seleccion = fileChooser.showOpenDialog(InterfazChat.this);
        
        if (seleccion == JFileChooser.APPROVE_OPTION && clienteTcp != null) {
            File archivoSeleccionado = fileChooser.getSelectedFile();
            
            // Avisamos en el chat
            agregarTextoAlChat("Tú (TCP): Enviaste un archivo\n", "estiloTu");
            
            // Mostramos miniatura si es imagen
            String nombreLower = archivoSeleccionado.getName().toLowerCase();
            if (nombreLower.endsWith(".png") || nombreLower.endsWith(".jpg") || nombreLower.endsWith(".jpeg")) {
                mostrarImagenEnChat(archivoSeleccionado.getAbsolutePath());
            }
            
            // Insertamos el botón interactivo en el chat
            agregarEnlaceArchivoChat(archivoSeleccionado);
            
            // Lo enviamos por la red
            clienteTcp.mandarArchivo(archivoSeleccionado, labelMetricas); 
        }
    }

    // Hilo Receptor P2P integrado (El oído de la ventana)
    private void iniciarEscuchaRespuestasUDP() {
        new Thread(() -> {
            try (DatagramSocket socketEscucha = new DatagramSocket(PUERTO_MI_ESCUCHA_UDP)) {
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                    socketEscucha.receive(paquete);
                    String mensajeRecibido = new String(paquete.getData(), 0, paquete.getLength(), StandardCharsets.UTF_8);
                    
                    // Pintar mensaje del amigo en burbuja gris pastel
                    agregarTextoAlChat("Amigo: " + mensajeRecibido + "\n", "estiloAmigo");
                }
            } catch (Exception e) { System.err.println("Oído UDP cerrado: " + e.getMessage()); }
        }).start();
    }

    // Agrega texto estilizado (burbuja pastel) de forma segura
    private void agregarTextoAlChat(String texto, String estiloNombre) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = panelChat.getStyledDocument();
                doc.insertString(doc.getLength(), texto, doc.getStyle(estiloNombre));
                // Separador suave entre mensajes
                doc.insertString(doc.getLength(), "\n", null); 
                panelChat.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) { e.printStackTrace(); }
        });
    }

    // Incrusta miniatura de imagen pastel en el flujo del chat
    private void mostrarImagenEnChat(String rutaImagen) {
        SwingUtilities.invokeLater(() -> {
            try {
                ImageIcon iconoOriginal = new ImageIcon(rutaImagen);
                Image img = iconoOriginal.getImage();
                // Escalar a 200px pastel
                Image imagenEscalada = img.getScaledInstance(200, -1, Image.SCALE_SMOOTH);
                
                panelChat.setCaretPosition(panelChat.getDocument().getLength());
                panelChat.insertIcon(new ImageIcon(imagenEscalada));
                
                panelChat.getStyledDocument().insertString(panelChat.getDocument().getLength(), "\n", null);
                panelChat.setCaretPosition(panelChat.getDocument().getLength());
            } catch (Exception e) { System.err.println("Error renderizando imagen: " + e.getMessage()); }
        });
    }

    // Incrusta un botón clickable que abre el archivo en tu sistema
    private void agregarEnlaceArchivoChat(File archivo) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Crear un botón que parezca un link de internet
                JButton btnLink = new JButton("📁 Abrir: " + archivo.getName());
                btnLink.setFont(new Font("Segoe UI", Font.BOLD, 13));
                btnLink.setForeground(new Color(0, 102, 204)); // Azul tipo link
                btnLink.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Manita de clic
                
                // Quitarle los bordes para que no parezca botón cuadrado
                btnLink.setContentAreaFilled(false);
                btnLink.setBorderPainted(false);
                btnLink.setFocusPainted(false);
                btnLink.setMargin(new Insets(0, 0, 0, 0));

                // Acción al darle clic: Abrir el archivo
                btnLink.addActionListener(e -> {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(archivo);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "No se pudo abrir el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                // Incrustar el botón en el chat
                panelChat.setCaretPosition(panelChat.getDocument().getLength());
                panelChat.insertComponent(btnLink);
                
                // Darle espacio después del botón
                panelChat.getStyledDocument().insertString(panelChat.getDocument().getLength(), "\n\n", null);
                panelChat.setCaretPosition(panelChat.getDocument().getLength());

            } catch (Exception e) {
                System.err.println("Error creando link de archivo: " + e.getMessage());
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new InterfazChat().setVisible(true);
        });
    }
}