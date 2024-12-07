
package edu.nyu.welcomehome.services;

import edu.nyu.welcomehome.models.Delivered;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Logger;

import static edu.nyu.welcomehome.utils.SqlFileLoader.loadSqlFromFile;

@Service
public class SuperviseService {
    private final Logger logger = Logger.getLogger(SuperviseService.class.getName());
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SuperviseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getOrderDetails(String username) {
        Map<String, Object> result = new HashMap<>();

        Map<String, String> params = Collections.singletonMap("username", username);
        String sql = loadSqlFromFile("sql/supervise/get-orders.sql", params);

        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            List<Map<String, Object>> orders = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> order = new HashMap<>();
                order.put("orderID", rs.getLong("orderID"));
                order.put("orderDate", rs.getString("orderDate"));
                order.put("orderNotes", rs.getString("orderNotes"));
                order.put("supervisor", rs.getString("supervisor"));
                order.put("client", rs.getString("client"));
                order.put("orderStatus", rs.getString("orderStatus"));
                order.put("currentStatus", rs.getString("currentStatus"));
                orders.add(order);
            }
            result.put("orders", orders);
        } catch (Exception e) {
            logger.severe("Error getting order details: " + e.getMessage());
            throw new RuntimeException("Failed to get order details", e);
        }

        return result;
    }

    @Transactional
    public boolean updateDeliveryStatus(Delivered delivered) {
        try {
            // Check delivery status
            Map<String, String> checkParams = Collections.singletonMap("orderID", String.valueOf(delivered.getOrderID()));
            String checkSql = loadSqlFromFile("sql/supervise/check-delivery-status.sql", checkParams);

            String currentStatus = null;
            try (Connection conn = jdbcTemplate.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(checkSql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    currentStatus = rs.getString("deliveredStatus");
                }
            }

            logger.info("Current status of order " + delivered.getOrderID() + ": " + currentStatus);

            if (!"NOT YET DELIVERED".equals(currentStatus)) {
                logger.warning("Order " + delivered.getOrderID() + " is not in 'NOT YET DELIVERED' status.");
                return false;
            }

            // Update to IN_TRANSIT
            Map<String, String> updateParams = new HashMap<>();
            updateParams.put("date", delivered.getDate());
            updateParams.put("orderID", String.valueOf(delivered.getOrderID()));

            String updateSql = loadSqlFromFile("sql/supervise/update-delivery-status.sql", updateParams);

            try (Connection conn = jdbcTemplate.getDataSource().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    logger.info("Successfully updated the status of order " + delivered.getOrderID() + " to 'IN_TRANSIT'.");
                    return true;
                }
                return false;
            }

        } catch (Exception e) {
            logger.severe("Error updating delivery status: " + e.getMessage());
            return false;
        }
    }
}
