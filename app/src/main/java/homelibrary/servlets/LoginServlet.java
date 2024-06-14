package homelibrary.servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * Servlet that 
 * 
 */
public class LoginServlet extends HttpServlet
{
    /**
     * Default constructor.
     */
    public LoginServlet()
    {
        super();
    }

    /**
     * Processes requests for both HTTP GET and POST methods.
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

        String login = request.getParameter("login");
        String password = request.getParameter("password");
        String exception = "";
        boolean success = false;
        try
        {
            success = checkLoginData(login, password);
        }
        catch (SQLException sql)
        {
            exception = String.format("<p>Cause: %s</p>", sql.getMessage());
        }

        // ---------------------------- Respond. ---------------------------- //
        
        if (success)
        {
            /* If the login was successful, go to the homepage (via HomeServlet).
               Remember the username as an attribute of the session. */

            HttpSession session = request.getSession(true);
            session.setAttribute("username", login);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/home");
            dispatcher.forward(request, response);
        }
        else
        {
            /* If the login was insuccessful, return a page containing a form
               for logging again. */

            response.setContentType("text/html;charset=UTF-8");
            try (PrintWriter out = response.getWriter())
            {
                out.println("""
                <!DOCTYPE html>
                <html>
                    <head>
                        <title>Home Library &middot; Welcome Page</title>
                        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                        <style>
                            .DefaultBox
                            {
                                border: 2px solid black;
                                margin: 20px;
                                padding: 20px;
                                width: 40%%;
                            }
                        </style>
                    </head>
                    <body>
                        <h1>Home Library &middot; Welcome!</h1>
                        <h2>Log in again</h2>
                            %s
                        <div class="DefaultBox">
                            <form action="login" method="post">
                                <table>
                                    <tr>
                                        <th align="right">Login:</th>
                                        <td><input name="login" type="text" required/></td>
                                    </tr>
                                    <tr>
                                        <th align="right">Password:</th>
                                        <td><input name="password" type="password" required/></td>
                                    </tr>
                                </table><br>
                                <input type="submit" value="Submit"/>
                            </form><br>
                            <p>If you do not have an account yet, <a href="register.html">register</a>.</p>
                            <p>See <a href="help.html">help</a>.</p>
                        </div>
                    </body>
                </html>
                """.formatted(exception));
            }
        }

    }

    /**
     * Checks if the login data are correct.
     *
     * @param login the login
     * @param password the password
     * @return true if the login data are correct, false otherwise
     * @throws SQLException if an SQL error occurs
     */
    private boolean checkLoginData(String login, String password) throws SQLException
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
                               AND
                                   u.password = '%s'
                           """.formatted(login, password);
            ResultSet results = statement.executeQuery(query);
            success = results.next();
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
