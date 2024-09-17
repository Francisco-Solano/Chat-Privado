module com.example.servidorproyectoservicios2ev {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.servidorproyectoservicios2ev to javafx.fxml;
    exports com.example.servidorproyectoservicios2ev;
}