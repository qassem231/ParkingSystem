package server;

import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import servergui.ServerMainController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import common.ClientRequest;
import common.Order;

public class ServerController extends AbstractServer {

	private Map<ConnectionToClient, String[]> clientInfoMap = new ConcurrentHashMap<>();
	private DBController db = new DBController();

	private ServerMainController guiController;

	public ServerController(int port, ServerMainController guiController) {
	    super(port);
	    this.guiController = guiController;
	}


	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
	    if (msg instanceof ClientRequest request) {
	        try {
	            String command = request.getCommand();
	            Object[] params = request.getParams();

	            switch (command) {
	                case "GET_ALL_ORDERS":
	                    List<Order> orders = db.getAllOrders();
	                    client.sendToClient(orders);
	                    break;

	                case "UPDATE_PARKING_SPACE":
	                    int orderId = Integer.parseInt(params[0].toString());
	                    int newSpace = Integer.parseInt(params[1].toString());
	                    db.updateParkingSpace(orderId, newSpace);
	                    client.sendToClient("✅ Parking space updated.");
	                    break;

	                case "UPDATE_ORDER_DATE":
	                    int orderIdDate = Integer.parseInt(params[0].toString());
	                    java.sql.Date newDate = java.sql.Date.valueOf(params[1].toString());
	                    db.updateOrderDate(orderIdDate, newDate);
	                    client.sendToClient("✅ Order date updated.");
	                    break;

	                case "DISCONNECT":
	                    client.close();
	                    break;

	                default:
	                    client.sendToClient("❌ Unknown command: " + command);
	            }

	        } catch (Exception e) {
	            System.err.println("❌ Error handling client request: " + e.getMessage());
	            e.printStackTrace();
	            try {
	                client.sendToClient("❌ Error processing command: " + e.getMessage());
	            } catch (IOException ioException) {
	                ioException.printStackTrace();
	            }
	        }
	    } else {
	        System.err.println("⚠️ Received unsupported message type: " + msg.getClass().getName());
	    }
	}



	@Override
	protected void clientConnected(ConnectionToClient client) {
	    String ip = client.getInetAddress().getHostAddress();
	    String host = client.getInetAddress().getHostName();
	    int id = client.hashCode();

	    clientInfoMap.put(client, new String[]{ip, host});

	    if (guiController != null) {
	        guiController.addClient(ip, host, id);
	    }

	    System.out.println("✅ Client connected: " + ip + " / " + host);
	}


	@Override
	protected synchronized void clientDisconnected(ConnectionToClient client) {
	    String[] info = clientInfoMap.getOrDefault(client, new String[]{"unknown", "unknown"});
	    String ip = info[0];
	    String host = info[1];
	    int id = client.hashCode();

	    if (guiController != null) {
	        guiController.updateClientStatus(id, "Disconnected");
	    }

	    System.out.println("❌ Client disconnected: " + ip + " / " + host);
	    clientInfoMap.remove(client);
	}
}
