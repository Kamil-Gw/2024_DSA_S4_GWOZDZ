package homelibrary.servlets;

import jakarta.servlet.RequestDispatcher;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet for registering a new user
 */
public class RegisterServlet extends HttpServlet
{
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        // --------------------- Do the business logic. --------------------- //

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password1 = request.getParameter("password");
        String password2 = request.getParameter("confirm");

        List<String> errorMessages = new ArrayList<>();

        boolean uniqueName = false;
        try
        {
            uniqueName = isUniqueUsername(username);
            if (!uniqueName)
            {
                errorMessages.add("The username is already in use.");
            }
        }
        catch (SQLException sql)
        {
            errorMessages.add(sql.getMessage());
        }

        boolean correctPassword = isCorrectPassword(password1);
        if (!correctPassword)
        {
            errorMessages.add("""
                              The password is incorrect; the requirements are:<br>
                              <ul>
                                  <li>at least 8 characters <b>and</b></li>
                                  <li>at least one lowercase letter <b>and</b></li>
                                  <li>at least one uppercase letter <b>and</b></li>
                                  <li>at least one digit <b>and</b></li>
                                  <li>at least one special character.</li>
                              </ul>
                              """);
        }

        boolean matchingPasswords = password2.equals(password1);
        if (!matchingPasswords)
        {
            errorMessages.add("The passwords do not match.");
        }

        boolean uniqueEmail = false;
        try
        {
            uniqueEmail = isUniqueEmail(email);
            if (!uniqueEmail)
            {
                errorMessages.add("The email is already in use.");
            }
        }
        catch (SQLException sql)
        {
            errorMessages.add(sql.getMessage());
        }

        if (uniqueName && correctPassword && matchingPasswords && uniqueEmail)
        {
            try
            {
                if (!registerUser(username, email, password1))
                {
                    errorMessages.add("Other problem occurred.");
                }
            }
            catch (SQLException sql)
            {
                errorMessages.add(sql.getMessage());
            }
        }
        for (int i = 0; i < errorMessages.size(); ++i)
        {
            int j = i + 1;
            while (j < errorMessages.size())
            {
                if (errorMessages.get(i).equals(errorMessages.get(j)))
                {
                    errorMessages.remove(j);
                }
                else
                {
                    ++j;
                }
            }
        }

        // ---------------------------- Respond. ---------------------------- //
        if (errorMessages.isEmpty())
        {
            HttpSession session = request.getSession(true);
            session.setAttribute("username", username);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/home");
            dispatcher.forward(request, response);
        }
        else
        {
            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter())
            {
                StringBuilder errorsHtml = new StringBuilder();
                for (var message : errorMessages)
                {
                    errorsHtml.append("""
                                      <div class="Error">%s</div><br>
                                      """.formatted(message));
                }
                out.println("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Home Library &middot; Register Again</title>
                    <style>
                        .DefaultBox
                        {
                            border: 2px solid black;
                            margin: 20px;
                            padding: 20px;
                            width: 40%%;
                        }
                        .Error
                        {
                            border: 2px solid #D00000;
                            margin: 10 px;
                            padding: 10 px;
                            width: 40%%;
                        }
                    </style>
                </head>
                <body>
                    <h1>Home Library &middot; Registration</h1>
                    <p>Errors:</p>
                    %s
                    <h2>Try again</h2>
                    <div class="DefaultBox">
                        <form action="register" method="post">
                            <table>
                                <tr>
                                    <th align="right">Username:</th>
                                    <td><input name="username" type="text" required/></td>
                                </tr>
                                <tr>
                                    <th align="right">Email:</th>
                                    <td><input name="email" type="text" required/></td>
                                </tr>
                                <tr>
                                    <th align="right">Password:</th>
                                    <td><input name="password" type="password" required/></td>
                                </tr>
                                <tr>
                                    <th align="right">Confirm:</th>
                                    <td><input name="confirm" type="password" required/></td>
                                </tr>
                            </table><br>
                            <input name="accept" type="checkbox" required/>I accept the regulations and terms.<br>
                            <input type="submit" value="Register"/>
                        </form><br>
                        <p>If you already have an account, <a href="index.html">log in</a>.</p>
                    </div>
                </body>
                </html>
                """.formatted(errorsHtml.toString()));
            }
        }
    }

    /**
     * Checks if the password is correct
     *
     * @param password the password to check
     * @return true if the password is correct, false otherwise
     */
    private boolean isCorrectPassword(String password)
    {
        boolean correct;
        if (password.length() < 8)
        {
            correct = false;
        }
        else
        {
            boolean lowercase = false;
            boolean uppercase = false;
            boolean digit = false;
            boolean special = false;
            for (int i = 0; i < password.length(); ++i)
            {
                char character = password.charAt(i);
                if (character >= 'a' && character <= 'z')
                {
                    lowercase = true;
                }
                else if (character >= 'A' && character <= 'Z')
                {
                    uppercase = true;
                }
                else if (character >= '0' && character <= '9')
                {
                    digit = true;
                }
                else if (character >= '!' && character <= '~')
                {
                    special = true;
                }
                if (lowercase && uppercase && digit && special)
                {
                    break;
                }
            }
            correct = lowercase && uppercase && digit && special;
        }
        return correct;
    }

    /**
     * Checks if the username is unique
     *
     * @param username the username to check
     * @return true if the username is unique, false otherwise
     * @throws SQLException
     */
    private boolean isUniqueUsername(String username) throws SQLException
    {
        boolean success;
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            String query = """
                               SELECT
                                       *
                               FROM
                                       app.users u
                               WHERE
                                       u.username = '%s'
                               """.formatted(username);
            ResultSet results = statement.executeQuery(query);
            success = !results.next();
        }
        return success;
    }

    /**
     * Checks if the email is unique
     *
     * @param email the email to check
     * @return true if the email is unique, false otherwise
     * @throws SQLException
     */
    private boolean isUniqueEmail(String email) throws SQLException
    {
        boolean success = false;
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            String query = """
                               SELECT
                                       *
                               FROM
                                       app.users u
                               WHERE
                                       u.email = '%s'
                               """.formatted(email);
            ResultSet results = statement.executeQuery(query);
            success = !results.next();
        }
        return success;
    }

    /**
     * Registers a new user
     *
     * @param username the username of the user
     * @param email the email of the user
     * @param password the password of the user
     * @return true if the user was registered successfully, false otherwise
     * @throws SQLException
     */
    private boolean registerUser(String username, String email, String password) throws SQLException
    {
        boolean success = false;
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            String query = """
                           INSERT INTO
                                   app.users
                                   (
                                       username,
                                       email,
                                       password,
                                       role
                                   )
                           VALUES
                                   (
                                       '%s',
                                       '%s',
                                       '%s',
                                       'user'
                                    )
                           """.formatted(username, email, password);
            int rows = statement.executeUpdate(query);
            success = (rows == 1);
        }
        return success;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

}
