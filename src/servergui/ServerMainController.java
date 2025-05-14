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

    // Observable list to hold and update client connection info
    private ObservableList<ClientInfo> clients = FXCollections.observableArrayList();

    /**
     * Initializes the controller when the GUI loads.
     * Fills default values for DB and server fields, sets up table columns.
     */
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

        // Set up table columns to bind to ClientInfo properties
        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));
        hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        clientTable.setItems(clients);
    }

    /**
     * Handles the Connect button click.
     * Tries to connect to the MySQL database and then starts the server.
     */
    @FXML
    void handleConnect() {
    	if (server != null && server.isListening()) {
            statusLabel.setText("⚠️ Server is already running.");
            return;
        }
        String dbIp = dbIpField.getText();
        String dbPort = dbPortField.getText();
        String user = dbUserField.getText();
        String pass = dbPassField.getText();
        int serverPort = Integer.parseInt(serverPortField.getText());

        // Create JDBC URL from user input
        String jdbcUrl = "jdbc:mysql://" + dbIp + ":" + dbPort + "/parking_db?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";

        try {
            // Test DB connection
            Connection conn = DriverManager.getConnection(jdbcUrl, user, pass);
            conn.close();

            statusLabel.setText("✅ DB connected. Starting server...");

            // Start the server
            server = new ServerController(serverPort, this);
            server.listen();

            statusLabel.setText("✅ Server running on port " + serverPort);
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the Disconnect button click.
     * Stops the server and clears the reference.
     */
    @FXML
    void handleDisconnect() {
        if (server != null) {
            try {
                server.close();
                server = null;
                statusLabel.setText("🔴 Server stopped running.");
            } catch (Exception e) {
                statusLabel.setText("❌ Failed to stop server.");
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("⚠️ Server is not running.");
        }
    }

    /**
     * Handles the Exit button click.
     * Terminates the JavaFX application.
     */
    @FXML
    void handleExit() {
        System.exit(0);
    }

    /**
     * Adds a newly connected client to the TableView.
     * @param ip Client IP
     * @param host Client hostname
     * @param id Internal unique identifier (hash code)
     */
    public void addClient(String ip, String host, int id) {
        Platform.runLater(() -> {
            for (ClientInfo client : clients) {
                if (client.getIp().equals(ip) && client.getHost().equals(host)) {
                    client.setStatus("Connected");
                    clientTable.refresh();
                    return;
                }
            }
            // If not found, add new
            clients.add(new ClientInfo(ip, host, "Connected", id));
        });
    }

    /**
     * Updates the status of a client in the TableView (e.g., to "Disconnected").
     * @param id Internal unique identifier (hash code)
     * @param status New status string
     */
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
