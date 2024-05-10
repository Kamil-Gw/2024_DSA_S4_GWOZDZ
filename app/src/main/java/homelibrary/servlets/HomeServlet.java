package homelibrary.servlets;

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

/**
 *
 * @author Kay Jay O'Nail
 */
public class HomeServlet extends HttpServlet
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

        addIdAsAttribute(request);

        // ---------------------------- Respond. ---------------------------- //
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter())
        {
            out.println("""
            <!DOCTYPE html>
            <html>
                <head>
                    <title>Home Library &middot; Homepage</title>
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
                    <h1>Home</h1>
                    <h2>Hello, %s!</h2>
                    <div>
                        <form action="search">
                            <input name="search" type="text" placeholder="Search"/>
                            <input type="submit" value="Search"/>
                        </form>
                    </div>
                    <div class="DefaultBox">
                        <p><a href="browse">Click</a> to browse your collection.</p>
                        <p><a href="notifications">Click</a> to view your notifications.</p>
                        <p><a href="bookshelf">Click</a> to open your bookshelf settings.</p>
                        <p><a href="account">Click</a> to go to account settings.</p>
                        <p><a href="to-be-named-yet">Click</a> to add a book to your bookshelf.</p>
                    </div>
                </body
            </html>
            """.formatted(getUsername(request)));
        }
    }

    private String getUsername(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        return (session != null) ? (String) session.getAttribute("username") : null;
    }

    private void addIdAsAttribute(HttpServletRequest request)
    {
        try
        {
            HttpSession session = request.getSession(false);
            if (session != null)
            {
                Driver driver = new org.postgresql.Driver();
                DriverManager.registerDriver(driver);

                String dbUrl = DatabaseConnectionData.DATABASE_URL;
                String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
                String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

                try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                     Statement statement = connection.createStatement())
                {
                    String username = getUsername(request);
                    String query = """
                                   SELECT
                                           u.id
                                   FROM
                                           app.users u
                                   WHERE
                                           u.username = '%s'
                                   """.formatted(username);
                    ResultSet results = statement.executeQuery(query);
                    if (results.next())
                    {
                        int id = results.getInt("id");
                        session.setAttribute("id", String.valueOf(id));
                    }
                }
            }
        }
        catch (SQLException e)
        {

        }
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
