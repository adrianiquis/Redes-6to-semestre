package vista;

import cliente.tcp.ClienteTCP;
import cliente.udp.ClienteEnviaUDP2;
import servidor.tcp.ServidorTCP;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.CRC32;

public class InterfazChat extends JFrame {

    // Componentes Visuales
    private JTextPane panelChat;
    private JTextField campoMensaje, campoIPDestino;
    private JButton btnEnviarTexto, btnEnviarArchivo, btnConectar;
    private JLabel labelMetricas, labelEstadoRed;

    // Motores de Red
    private ClienteTCP clienteTcp;
    private ClienteEnviaUDP2 clienteUdp;
    private ServidorTCP servidorTcp;

    // Puertos P2P
    private final int PUERTO_TCP_AMIGO      = 6001;
    private final int PUERTO_UDP_AMIGO      = 5001;
    private final int PUERTO_MI_ESCUCHA_UDP = 5001;

    // Paleta de colores
    private final Color COLOR_FONDO_VENTANA  = new Color(240, 248, 255);
    private final Color COLOR_PANEL_CHAT     = Color.WHITE;
    private final Color COLOR_PRIMARIO_PASTEL = new Color(174, 217, 224);
    private final Color COLOR_BURBUJA_USUARIO = new Color(209, 255, 209);
    private final Color COLOR_BURBUJA_AMIGO   = new Color(255, 209, 220);
    private final Color COLOR_TEXTO_OSCURO    = new Color(60, 60, 60);
    private final Font  FUENTE_MODERNA        = new Font("Segoe UI", Font.PLAIN, 14);

    public InterfazChat() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        setTitle("Chat Redes P2P");
        setSize(700, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(COLOR_FONDO_VENTANA);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // ── Panel superior: IP + botón conectar ──────────────────────────────
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

        btnConectar = crearBotonEstilizado("Establecer Red", COLOR_PRIMARIO_PASTEL);

        labelEstadoRed = new JLabel("● Desconectado");
        labelEstadoRed.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelEstadoRed.setForeground(Color.RED);

        panelIP.add(lblIP);
        panelIP.add(campoIPDestino);
        panelIP.add(btnConectar);
        panelIP.add(labelEstadoRed);
        panelNorte.add(panelIP, BorderLayout.CENTER);
        add(panelNorte, BorderLayout.NORTH);

        // ── Panel central: chat ───────────────────────────────────────────────
        panelChat = new JTextPane();
        panelChat.setEditable(false);
        panelChat.setBackground(COLOR_PANEL_CHAT);
        configurarEstilosChat();

        JScrollPane scrollChat = new JScrollPane(panelChat);
        scrollChat.setBorder(new EmptyBorder(10, 15, 10, 15));
        scrollChat.getViewport().setBackground(COLOR_PANEL_CHAT);
        add(scrollChat, BorderLayout.CENTER);

        // ── Panel inferior: entrada + botones ────────────────────────────────
        JPanel panelSur = new JPanel(new BorderLayout(10, 10));
        panelSur.setBackground(COLOR_FONDO_VENTANA);
        panelSur.setBorder(new EmptyBorder(0, 15, 15, 15));

        JPanel panelEntrada = new JPanel(new BorderLayout(5, 0));
        panelEntrada.setBackground(COLOR_FONDO_VENTANA);
        campoMensaje = new JTextField();
        campoMensaje.setFont(FUENTE_MODERNA);
        campoMensaje.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        btnEnviarTexto = crearBotonEstilizado("Enviar (UDP)", COLOR_PRIMARIO_PASTEL);
        btnEnviarTexto.setEnabled(false);

        panelEntrada.add(campoMensaje, BorderLayout.CENTER);
        panelEntrada.add(btnEnviarTexto, BorderLayout.EAST);

        JPanel panelAccionesExtra = new JPanel(new BorderLayout(10, 0));
        panelAccionesExtra.setBackground(COLOR_FONDO_VENTANA);

        btnEnviarArchivo = crearBotonEstilizado("Adjuntar Archivo/Foto (TCP)", new Color(255, 228, 225));
        btnEnviarArchivo.setEnabled(false);

        labelMetricas = new JLabel(" Metricas TCP: Esperando transferencia...");
        labelMetricas.setFont(new Font("Consolas", Font.BOLD, 12));
        labelMetricas.setForeground(new Color(100, 100, 100));

        panelAccionesExtra.add(btnEnviarArchivo, BorderLayout.WEST);
        panelAccionesExtra.add(labelMetricas, BorderLayout.CENTER);

        panelSur.add(panelEntrada, BorderLayout.NORTH);
        panelSur.add(panelAccionesExtra, BorderLayout.SOUTH);
        add(panelSur, BorderLayout.SOUTH);

        // ── Eventos ───────────────────────────────────────────────────────────
        btnConectar.addActionListener(e -> inicializarRedDinamica());
        btnEnviarTexto.addActionListener(e -> enviarTexto());
        campoMensaje.addActionListener(e -> enviarTexto());
        btnEnviarArchivo.addActionListener(e -> seleccionarYEnviarArchivo());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────────────────

    private JButton crearBotonEstilizado(String texto, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(colorFondo);
        boton.setForeground(COLOR_TEXTO_OSCURO);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1),
            new EmptyBorder(8, 20, 8, 20)
        ));
        return boton;
    }

    private void configurarEstilosChat() {
        StyledDocument doc = panelChat.getStyledDocument();
        Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style estiloTu = doc.addStyle("estiloTu", def);
        StyleConstants.setForeground(estiloTu, COLOR_TEXTO_OSCURO);
        StyleConstants.setBackground(estiloTu, COLOR_BURBUJA_USUARIO);
        StyleConstants.setBold(estiloTu, true);

        Style estiloAmigo = doc.addStyle("estiloAmigo", def);
        StyleConstants.setForeground(estiloAmigo, Color.BLACK);
        StyleConstants.setBackground(estiloAmigo, COLOR_BURBUJA_AMIGO);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Red
    // ─────────────────────────────────────────────────────────────────────────

    private void inicializarRedDinamica() {
        String ipTarget = campoIPDestino.getText().trim();
        if (ipTarget.isEmpty() || ipTarget.split("\\.").length != 4) {
            JOptionPane.showMessageDialog(this,
                "Por favor ingresa una IP IPv4 valida (ej. 10.10.28.145).",
                "IP Incorrecta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            labelEstadoRed.setText("● Conectando...");
            labelEstadoRed.setForeground(Color.ORANGE);

            if (clienteUdp != null) clienteUdp.detener();

            // Servidor TCP para recibir archivos — se inicia solo una vez
            if (servidorTcp == null) {
                servidorTcp = new ServidorTCP(PUERTO_TCP_AMIGO);
                servidorTcp.inicia(this::recibirArchivoPorTCP);
            }

            // Cliente TCP para enviar archivos
            clienteTcp = new ClienteTCP(ipTarget, PUERTO_TCP_AMIGO);
            clienteTcp.inicia();

            // Cliente UDP para enviar mensajes
            clienteUdp = new ClienteEnviaUDP2(new DatagramSocket(), ipTarget, PUERTO_UDP_AMIGO);
            clienteUdp.start();

            // Receptor UDP para recibir mensajes
            iniciarEscuchaRespuestasUDP();

            String miIP = obtenerIPLocal();
            labelEstadoRed.setText("● Conectado a " + ipTarget
                + "  |  Tu IP: " + miIP
                + "  (UDP " + PUERTO_MI_ESCUCHA_UDP + " / TCP " + PUERTO_TCP_AMIGO + ")");
            labelEstadoRed.setForeground(new Color(0, 180, 0));
            campoIPDestino.setEnabled(false);
            btnConectar.setEnabled(false);
            btnEnviarTexto.setEnabled(true);
            btnEnviarArchivo.setEnabled(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error de red: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            labelEstadoRed.setText("● Error");
            labelEstadoRed.setForeground(Color.RED);
        }
    }

    private void enviarTexto() {
        String texto = campoMensaje.getText().trim();
        if (!texto.isEmpty() && clienteUdp != null) {
            agregarTextoAlChat("Tu: " + texto + "\n", "estiloTu");
            clienteUdp.encolarMensajeParaEnvio(texto);
            campoMensaje.setText("");
        }
    }

    private void seleccionarYEnviarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        int seleccion = fileChooser.showOpenDialog(this);

        if (seleccion == JFileChooser.APPROVE_OPTION && clienteTcp != null) {
            File archivo = fileChooser.getSelectedFile();

            agregarTextoAlChat("Tu (TCP): " + archivo.getName()
                + " (" + archivo.length() + " bytes)\n", "estiloTu");

            String nombreLower = archivo.getName().toLowerCase();
            if (nombreLower.endsWith(".png") || nombreLower.endsWith(".jpg") || nombreLower.endsWith(".jpeg")) {
                mostrarImagenEnChat(archivo.getAbsolutePath());
            }

            agregarEnlaceArchivoChat(archivo);
            clienteTcp.mandarArchivo(archivo, labelMetricas);
        }
    }

    // Receptor UDP: desempaqueta writeUTF + writeLong y verifica CRC32 (Req. 6)
    private void iniciarEscuchaRespuestasUDP() throws Exception {
        DatagramSocket socketEscucha = new DatagramSocket(PUERTO_MI_ESCUCHA_UDP);
        Thread hilo = new Thread(() -> {
            byte[] buffer = new byte[65507]; // máximo payload UDP sobre IPv4
            try {
                while (true) {
                    DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
                    socketEscucha.receive(paquete);

                    try (DataInputStream dis = new DataInputStream(
                            new ByteArrayInputStream(paquete.getData(), 0, paquete.getLength()))) {

                        String texto           = dis.readUTF();
                        long  checksumRecibido = dis.readLong();

                        // Verificar CRC32
                        CRC32 crc = new CRC32();
                        crc.update(texto.getBytes(StandardCharsets.UTF_8));
                        boolean crcOk = crc.getValue() == checksumRecibido;

                        String prefijo = crcOk ? "Amigo" : "Amigo [CRC ERROR]";
                        agregarTextoAlChat(prefijo + ": " + texto + "\n", "estiloAmigo");
                    }
                }
            } catch (Exception e) {
                if (!socketEscucha.isClosed())
                    System.err.println("Receptor UDP cerrado: " + e.getMessage());
            } finally {
                socketEscucha.close();
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    // Callback invocado desde el servidor TCP cuando llega un archivo
    private void recibirArchivoPorTCP(File archivo) {
        agregarTextoAlChat("Amigo (TCP): " + archivo.getName()
            + " (" + archivo.length() + " bytes)\n", "estiloAmigo");
        String nombreLower = archivo.getName().toLowerCase();
        if (nombreLower.endsWith(".png") || nombreLower.endsWith(".jpg") || nombreLower.endsWith(".jpeg")) {
            mostrarImagenEnChat(archivo.getAbsolutePath());
        }
        agregarEnlaceArchivoChat(archivo);
    }

    // Devuelve la primera IP local no-loopback para compartirla con el amigo
    private String obtenerIPLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (!addr.getHostAddress().contains(":")) // saltar IPv6
                        return addr.getHostAddress();
                }
            }
        } catch (Exception ignored) {}
        return "desconocida";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Renderizado del chat
    // ─────────────────────────────────────────────────────────────────────────

    private void agregarTextoAlChat(String texto, String estiloNombre) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = panelChat.getStyledDocument();
                doc.insertString(doc.getLength(), texto, doc.getStyle(estiloNombre));
                panelChat.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }

    private void mostrarImagenEnChat(String rutaImagen) {
        SwingUtilities.invokeLater(() -> {
            try {
                ImageIcon iconoOriginal = new ImageIcon(rutaImagen);
                Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(200, -1, Image.SCALE_SMOOTH);
                panelChat.setCaretPosition(panelChat.getDocument().getLength());
                panelChat.insertIcon(new ImageIcon(imagenEscalada));
                panelChat.getStyledDocument().insertString(panelChat.getDocument().getLength(), "\n", null);
                panelChat.setCaretPosition(panelChat.getDocument().getLength());
            } catch (Exception e) {
                System.err.println("Error mostrando imagen: " + e.getMessage());
            }
        });
    }

    private void agregarEnlaceArchivoChat(File archivo) {
        SwingUtilities.invokeLater(() -> {
            try {
                JButton btnLink = new JButton("Abrir: " + archivo.getName());
                btnLink.setFont(new Font("Segoe UI", Font.BOLD, 13));
                btnLink.setForeground(new Color(0, 102, 204));
                btnLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btnLink.setContentAreaFilled(false);
                btnLink.setBorderPainted(false);
                btnLink.setFocusPainted(false);
                btnLink.setMargin(new Insets(0, 0, 0, 0));
                btnLink.addActionListener(e -> {
                    try {
                        if (Desktop.isDesktopSupported())
                            Desktop.getDesktop().open(archivo);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "No se pudo abrir el archivo.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                panelChat.setCaretPosition(panelChat.getDocument().getLength());
                panelChat.insertComponent(btnLink);
                panelChat.getStyledDocument().insertString(panelChat.getDocument().getLength(), "\n", null);
                panelChat.setCaretPosition(panelChat.getDocument().getLength());
            } catch (Exception e) {
                System.err.println("Error creando enlace: " + e.getMessage());
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfazChat().setVisible(true));
    }
}
