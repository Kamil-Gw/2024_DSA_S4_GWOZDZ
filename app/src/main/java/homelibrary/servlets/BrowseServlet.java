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
public class BrowseServlet extends HttpServlet
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

        String ownerId = getOwnerId(request);
        String tableHtml = getTableOfPublications(ownerId);
        
        // ---------------------------- Respond. ---------------------------- //
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter())
        {
            out.println("""
                        <html>
                        <head>
                            <title>Home Library &middot; User's Publications</title>
                            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                        </head>
                        <body>
                            <h1>Home Library &middot; Your Publications:</h1>
                            %s<br>
                            <p><a href="add">Add</a> a book.</p>
                        </body>
                        </html>
                        """.formatted(tableHtml));
        }
    }
    
    private String getOwnerId(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        return (session != null) ? (String) session.getAttribute("id") : null;
    }

    // @TODO Maciek -> przygotować tabelkę
    private String getTableOfPublications(String ownerId)
    {
        StringBuilder tableHtml = new StringBuilder("<p><b>Your publications:</b></p>");
        try
        {
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
                                       p.title,
                                       EXTRACT (YEAR FROM p.publication_date) AS "year",
                                       p.isbn,
                                       p.issn,
                                       a.name,
                                       a.surname
                               FROM
                                       app.publications p
                                   JOIN
                                       app.authorships pa ON p.id = pa.publication_id
                                   JOIN
                                       app.authors a ON pa.author_id = a.id
                               WHERE
                                       p.owner_id = %s
                               """.formatted(ownerId);
                ResultSet results = statement.executeQuery(query);
                if (results.next())
                {
                    tableHtml.append("""
                                     <table border="1">
                                         <tr>
                                             <th>Title</th>
                                             <th>Year of publication</th>
                                             <th>ISBN/ISSN</th>
                                             <th></th>
                                             <th></th>
                                             <th></th>
                                         </tr>
                                     """);
                    do
                    {
                        String title = results.getString("title");
                        tableHtml.append("""
                                         <tr>
                                            <td>%s</td>
                                         </tr>
                                         """.formatted(title));
                    }
                    while (results.next());
                    tableHtml.append("</table>");
                }
                else
                {
                    tableHtml.append("<p>You do not have any books yet. How about adding some?</p>");
                }
            }
        }
        catch (SQLException sql)
        {
            tableHtml.append(sql);
        }

        return tableHtml.toString();
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
