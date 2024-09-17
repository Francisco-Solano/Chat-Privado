package com.example.servidorproyectoservicios2ev;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class HelloController {

    @FXML
    private ListView<InetAddress> userListView;


    private ObservableList<InetAddress> usuariosConectados = FXCollections.observableArrayList();
    private ObservableList<InetAddress> observableUsuariosConectados;

    // Método de inicialización que recibe la lista de direcciones IP de clientes
    public void init(List<InetAddress> clientIPList) {

        observableUsuariosConectados = FXCollections.observableArrayList(clientIPList);
        usuariosConectados.addAll(observableUsuariosConectados);
        userListView.setItems(usuariosConectados);
    }

    // Método para agregar usuarios conectados
    public void agregarUsuarioConectado(List<InetAddress> ipAddress) {
        usuariosConectados.addAll(ipAddress);
        userListView.setItems(usuariosConectados);
        System.out.println("Usuario Conectado: ");

    }

    public void setUsuariosConectados(ObservableList<InetAddress> usuariosConectados) {
        this.usuariosConectados = usuariosConectados;
    }
}
