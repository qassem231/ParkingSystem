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

	// Map to keep track of connected clients and their IP/Host info
	private Map<ConnectionToClient, String[]> clientInfoMap = new ConcurrentHashMap<>();

	// Database access object
	private DBController db = new DBController();

	// Reference to the server GUI controller
	private ServerMainController guiController;

	/**
	 * Constructs the server controller with a specific port and GUI controller
	 * reference.
	 */
	public ServerController(int port, ServerMainController guiController) {
		super(port);
		this.guiController = guiController;
	}

	/**
	 * Handles incoming messages from clients. Processes ClientRequest objects and
	 * performs appropriate DB operations.
	 */
	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		if (msg instanceof ClientRequest request) {
			try {
				String command = request.getCommand();
				Object[] params = request.getParams();

				switch (command) {
				case "SEARCH_ORDERS_BY_SUBSCRIBER_ID":
					int subscriberId = Integer.parseInt(params[0].toString());
					List<Order> results = db.getOrdersBySubscriberId(subscriberId);
					client.sendToClient(results);
					break;
				case "GET_ALL_ORDERS":
					List<Order> orders = db.getAllOrders();
					client.sendToClient(orders);
					break;

				case "UPDATE_PARKING_SPACE":
				    int orderId = Integer.parseInt(params[0].toString());
				    int newSpace = Integer.parseInt(params[1].toString());
				    boolean updatedSpace = db.updateParkingSpace(orderId, newSpace);
				    if (updatedSpace) {
				        client.sendToClient("✅ Parking space updated.");
				    } else {
				        client.sendToClient("❌ Order " + orderId + " not found. Parking space not updated.");
				    }
				    break;

				case "UPDATE_ORDER_DATE":
				    int orderIdDate = Integer.parseInt(params[0].toString());
				    java.sql.Date newDate = java.sql.Date.valueOf(params[1].toString());
				    boolean updatedDate = db.updateOrderDate(orderIdDate, newDate);
				    if (updatedDate) {
				        client.sendToClient("✅ Order date updated.");
				    } else {
				        client.sendToClient("❌ Order " + orderIdDate + " not found. Date not updated.");
				    }
				    break;

				case "DISCONNECT":
					client.close(); // Client explicitly asked to disconnect
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

	/**
	 * Triggered when a new client connects to the server. Extracts IP and hostname
	 * info and updates the GUI.
	 */
	@Override
	protected void clientConnected(ConnectionToClient client) {
		String ip = client.getInetAddress().getHostAddress();
		String host = client.getInetAddress().getCanonicalHostName();
		int id = client.hashCode();

		clientInfoMap.put(client, new String[] { ip, host });

		if (guiController != null) {
			guiController.addClient(ip, host, id);
		}

		System.out.println("✅ Client connected: " + ip + " / " + host);
	}

	/**
	 * Triggered when a client disconnects (either intentionally or unexpectedly).
	 * Updates the GUI and removes the client from the internal map.
	 */
	@Override
	protected synchronized void clientDisconnected(ConnectionToClient client) {
		String[] info = clientInfoMap.getOrDefault(client, new String[] { "unknown", "unknown" });
		String ip = info[0];
		String host = info[1];
		int id = client.hashCode();

		if (guiController != null) {
			guiController.updateClientStatus(id, "Disconnected");
		}

		System.out.println("❌ Client disconnected: " + ip + " / " + host);
		// clientInfoMap.remove(client);
	}
}
