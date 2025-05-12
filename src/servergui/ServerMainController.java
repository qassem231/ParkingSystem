package servergui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import server.ServerController;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;

public class ServerMainController {

    @FXML private TextField serverIpField, serverPortField, dbIpField, dbPortField, dbUserField;
    @FXML private PasswordField dbPassField;
    @FXML private Label statusLabel;
    @FXML private Button connectButton, disconnectButton;
    @FXML private TableView<ClientInfo> clientTable;
    @FXML private TableColumn<ClientInfo, String> ipColumn, hostColumn, statusColumn;

    private ServerController server;

    private ObservableList<ClientInfo> clients = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            serverIpField.setText(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            serverIpField.setText("Unknown");
        }
        dbIpField.setText("localhost");
        dbPortField.setText("3306");
        dbUserField.setText("root");
        dbPassField.setText("Aa123456");
        serverPortField.setText("5555");

        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));
        hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        clientTable.setItems(clients);
    }

    @FXML
    void handleConnect() {
        String dbIp = dbIpField.getText();
        String dbPort = dbPortField.getText();
        String user = dbUserField.getText();
        String pass = dbPassField.getText();
        int serverPort = Integer.parseInt(serverPortField.getText());

        String jdbcUrl = "jdbc:mysql://" + dbIp + ":" + dbPort + "/parking_db?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";

        try {
            Connection conn = DriverManager.getConnection(jdbcUrl, user, pass);
            conn.close();

            statusLabel.setText("✅ DB connected. Starting server...");

            server = new ServerController(serverPort, this);  // ← FIX: assign to field
            server.listen();

            statusLabel.setText("✅ Server running on port " + serverPort);
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleDisconnect() {
        if (server != null) {
            try {
                server.close();
                server = null;  // ← clear reference
                statusLabel.setText("🔴 Server stopped running.");
            } catch (Exception e) {
                statusLabel.setText("❌ Failed to stop server.");
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("⚠️ Server is not running.");
        }
    }

    @FXML
    void handleExit() {
        System.exit(0);
    }

    public void addClient(String ip, String host, int id) {
        Platform.runLater(() -> clients.add(new ClientInfo(ip, host, "Connected", id)));
    }

    public void updateClientStatus(int id, String status) {
        Platform.runLater(() -> {
            for (ClientInfo client : clients) {
                if (client.getId() == id) {
                    client.setStatus(status);
                    clientTable.refresh();
                    break;
                }
            }
        });
    }
}
