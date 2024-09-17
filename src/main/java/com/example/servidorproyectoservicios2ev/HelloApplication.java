package com.example.servidorproyectoservicios2ev;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HelloApplication extends Application {

    private static final int SERVER_PORT = 5010;
    private static final int BUFFER_SIZE = 1024;
    private static final List<InetAddress> CLIENT_IP_LIST = new ArrayList<>();
    private static final List<Integer> CLIENT_PORT_LIST = new ArrayList<>();
    private static final Set<String> USUARIOS_DISPONIBLES = new HashSet<>();
    private DatagramSocket serverSocket;

    // Método para inicializar el servidor
    public void initialize() {
        try {
            serverSocket = new DatagramSocket(SERVER_PORT);
            iniciarServidor(serverSocket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Configuración de la interfaz de usuario
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 720, 640);
        stage.setTitle("Server");
        stage.setScene(scene);
        stage.show();
        // Iniciamos el Servidor
        initialize();
    }

    // Método que inicia el servidor en un hilo separado
    private void iniciarServidor(DatagramSocket serverSocket) {
        new Thread(() -> {
            try {
                System.out.println("Servidor iniciado en el puerto " + SERVER_PORT);
                while (true) {
                    // Recibimos datos del cliente
                    byte[] receiveData = new byte[BUFFER_SIZE];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    InetAddress clientAddress = receivePacket.getAddress();
                    int clientPort = receivePacket.getPort();

                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    String[] messageParts = message.split("\\|");

                    // Verificamos el tipo de mensaje recibido
                    if (messageParts[0].equals("CHECK_USERNAME")) {
                        // Verificamos la disponibilidad del nombre de usuario
                        String username = messageParts[1];
                        if (!USUARIOS_DISPONIBLES.contains(username)) {
                            // Enviar respuesta al cliente
                            enviarRespuestaACliente(clientAddress, clientPort, "USERNAME_AVAILABLE");
                            // Agregar usuario a la lista de usuarios disponibles
                            USUARIOS_DISPONIBLES.add(username);
                            CLIENT_IP_LIST.add(clientAddress);
                            CLIENT_PORT_LIST.add(clientPort);
                            System.out.println("USUARIO CORRECTO Y ANADIDO A LA LISTA");

                            System.out.println("Cliente aceptado - PORT: " + clientPort + ", IP: " + clientAddress);
                        } else {
                            // Enviar respuesta al cliente
                            enviarRespuestaACliente(clientAddress, clientPort, "USERNAME_UNAVAILABLE");
                        }
                    } else if (messageParts[0].equals("TEXT")) {
                        // Procesar mensaje de texto
                        System.out.println("Invocando al metodo recieveMessage");
                        receiveMessage(message);
                    }
                }
            } catch (Exception e) {
                // Manejo de errores y cierre de la aplicación
                Platform.exit();
            }
        }).start();
    }

    // Método para enviar respuesta al cliente
    private void enviarRespuestaACliente(InetAddress clientAddress, int clientPort, String response) {
        try {
            byte[] sendData = response.getBytes();
            DatagramSocket responseSocket = new DatagramSocket();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            responseSocket.send(sendPacket);
            responseSocket.close();
            System.out.println("Respuesta enviada al cliente - IP: " + clientAddress + ", Puerto: " + clientPort + ", Respuesta: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para procesar un mensaje recibido
    private void receiveMessage(String message) {
        String[] messageParts = message.split("\\|");
        String receivedUserName = messageParts[1];
        String receivedMessage = messageParts[2];

        System.out.println("Mensaje recibido de " + receivedUserName + ": " + receivedMessage);

        if (receivedMessage.equals("STOP")) {
            // Detener el servidor si el mensaje recibido es "STOP"
            System.out.println("Servidor detenido...");
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            Platform.exit();
        } else {
            // Reenviar el mensaje a todos los clientes conectados
            enviarMensajeAClientesConectados(message);
        }
    }

    // Método para reenviar un mensaje a todos los clientes conectados
    private void enviarMensajeAClientesConectados(String message) {
        try {
            byte[] sendData = message.getBytes();
            DatagramSocket clientSocket = new DatagramSocket();
            // Recorremos la lista de IPs y enviamos el mensaje a cada cliente
            for (int i = 0; i < CLIENT_IP_LIST.size(); i++) {
                InetAddress clientAddress = CLIENT_IP_LIST.get(i);
                int clientPort = CLIENT_PORT_LIST.get(i);

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                clientSocket.send(sendPacket);
            }

            clientSocket.close();
            System.out.println("Mensaje reenviado a todos los clientes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método principal
    public static void main(String[] args) {
        // Iniciar la aplicación JavaFX
        launch();
    }
}
