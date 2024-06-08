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
 * Servlet that prints the homepage.
 * 
 */
public class HomeServlet extends HttpServlet
{

    /**
     * Prints the homepage.
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

        if (request.getAttribute("id") == null)
        {
            addIdAsAttribute(request);
        }

        // ---------------------------- Respond. ---------------------------- //
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter())
        {
            out.println("""
            <!DOCTYPE html>
            <HTML>
                <HEAD>
                    <TITLE>Home Library &middot; Homepage</TITLE>
                    <STYLE>
                        .DefaultBox
                        {
                            border: 2px solid black;
                            margin: 20px;
                            padding: 20px;
                            width: 40%%;
                        }
                    </STYLE>
                </HEAD>
                <BODY>
                    <H1>Home Library &middot; Homepage</H1>
                    <H2>Hello, %s!</H2>
                    %s
                    <DIV>
                        <FORM action="search">
                            <input name="search" type="text" placeholder="Search"/>
                            <input type="submit" value="Search"/>
                        </FORM>
                    </DIV>
                    <DIV class="DefaultBox">
                        <P><A href="browse">Click</A> to browse your collection.</P>
                        <P><A href="notifications">Click</A> to view your notifications.</P>
                        <P><A href="bookshelf">Click</A> to open your bookshelf settings.</P>
                        <P><A href="change-settings">Click</A> to go to account settings.</P>
                        <P><A href="addingtobookshelf">Click</A> to add a book to your bookshelf.</P>
                    </DIV>
                    <P>See <A href="help.html">help</A>.</P>
                </BODY>
            </HTML>
            """.formatted(getUsername(request), extractErrors(request)));
        }
    }

    /**
     * Reads the "username" attribute from the request.
     * 
     * @param request HTTP request to the servlet
     * @return username if the attribute was set, null otherwise
     */
    private String getUsername(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        return (session != null) ? (String) session.getAttribute("username") : null;
    }

    /**
     * Adds the user's ID as session attribute.
     * 
     * @param request HTTP request to the servlet
     */
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
    
    /**
     * Proofs whether attribute "error-messages" was set to the request and prepares
     * HTML description of the errors.
     * 
     * @param request HTTP request to the servlet
     * @return HTTP code describing the errors
     */
    private String extractErrors(HttpServletRequest request)
    {
        String errorMessages = (String) request.getAttribute("error-messages");
        request.removeAttribute("error-messages");
        
        StringBuilder errorsHtml = new StringBuilder();
        
        if (errorMessages != null)
        {
            String[] particularMessages = errorMessages.split(";");
            if (!(particularMessages.length == 1 && particularMessages[0].isBlank()))
            {
                errorsHtml.append("<P>");
                for (var message : particularMessages)
                {
                    errorsHtml.append(message).append("<BR/>");
                }
                errorsHtml.append("</P>");
            }
        }
        
        return errorsHtml.toString();
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
