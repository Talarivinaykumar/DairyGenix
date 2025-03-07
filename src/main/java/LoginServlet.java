import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;

public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/a71", "root", "526873");

            // Check if the user exists in the database
            String checkUserQuery = "SELECT * FROM user WHERE username = ?";
            PreparedStatement checkUserPst = con.prepareStatement(checkUserQuery);
            checkUserPst.setString(1, username);
            ResultSet rs = checkUserPst.executeQuery();

            if (!rs.next()) {
                // If user doesn't exist, redirect to signup page
                response.sendRedirect("login.html?message=User does not exist. Please sign up first.");
            } else {
                // Verify password
                String storedPassword = rs.getString("password");
                if (storedPassword.equals(password)) {
                    // Successful login, redirect to index.html
                    response.sendRedirect("index.html");
                } else {
                    // Incorrect password
                    response.sendRedirect("login.html?message=Incorrect password. Please try again.");
                }
            }

            rs.close();
            checkUserPst.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("Login failed! Please try again.");
        }
    }
}
