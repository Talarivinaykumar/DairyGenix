import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class OrderConfirmationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            sb.append(line);
        }

        JSONObject json = new JSONObject(sb.toString());
        String name = json.getString("name");
        String email = json.getString("email");
        String address = json.getString("address");
        String totalPrice = json.getString("totalPrice");
        JSONArray items = json.getJSONArray("items");

        try {
            // Database connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/a71", "root", "526873");

            // Insert order into the database
            String query = "INSERT INTO orders (name, email, address, total_price) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3, address);
            pst.setString(4, totalPrice);
            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                // Get the generated order ID
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1); // Fetch the order ID

                    // Insert order items into the database
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        String itemName = item.getString("name");
                        double itemPrice = item.getDouble("price");
                        int quantity = item.getInt("quantity");

                        String itemQuery = "INSERT INTO order_items (order_id, item_name, item_price, quantity) VALUES (?, ?, ?, ?)";
                        PreparedStatement itemStmt = con.prepareStatement(itemQuery);
                        itemStmt.setInt(1, orderId);
                        itemStmt.setString(2, itemName);
                        itemStmt.setDouble(3, itemPrice);
                        itemStmt.setInt(4, quantity);
                        itemStmt.executeUpdate();
                        itemStmt.close();
                    }
                }
                // Send confirmation email
                sendEmail(email, name, items, totalPrice);
                out.println("{\"success\": true}");
            } else {
                out.println("{\"success\": false}");
            }

            pst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("{\"success\": false}");
        } finally {
            out.close();
        }
    }

    private void sendEmail(String to, String name, JSONArray items, String totalPrice) {
        final String from = "your-email@gmail.com"; // Change to your email
        final String password = "your-email-password"; // Change to your email password

        // Set up the mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create a session
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Order Confirmation");

            // Create the email body
            StringBuilder emailBody = new StringBuilder();
            emailBody.append("Dear ").append(name).append(",\n\n");
            emailBody.append("Thank you for your order!\n");
            emailBody.append("Here are your order details:\n\n");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                emailBody.append(item.getString("name"))
                         .append(" - Rs ").append(item.getDouble("price"))
                         .append(" x ").append(item.getInt("quantity")).append("\n");
            }
            emailBody.append("\nTotal Amount: Rs ").append(totalPrice).append("\n");
            emailBody.append("\nBest regards,\nYour Company Name");

            message.setText(emailBody.toString());

            // Send message
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
