package server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import common.Order;

public class DBController {
	private static final String URL = "jdbc:mysql://localhost:3306/parking_db?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true"
			+ "";
	private static final String USER = "root"; // Replace with your MySQL username
	private static final String PASSWORD = "Aa123456"; // Replace with your MySQL password

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

	public List<Order> getAllOrders() {
		List<Order> orders = new ArrayList<>();
		String sql = "SELECT * FROM `Order`";

		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				orders.add(new Order(rs.getInt("order_number"), rs.getInt("parking_space"), rs.getDate("order_date"),
						rs.getInt("confirmation_code"), rs.getInt("subscriber_id"),
						rs.getDate("date_of_placing_an_order")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return orders;
	}

	public boolean updateOrderDate(int orderNumber, Date newDate) {
		String sql = "UPDATE `Order` SET order_date = ? WHERE order_number = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setDate(1, newDate);
			stmt.setInt(2, orderNumber);

			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0; // true if update was successful
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// ✅ Function to update parking_space
	public boolean updateParkingSpace(int orderNumber, int newParkingSpace) {
		String sql = "UPDATE `Order` SET parking_space = ? WHERE order_number = ?";

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, newParkingSpace);
			stmt.setInt(2, orderNumber);

			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0; // true if update was successful
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
