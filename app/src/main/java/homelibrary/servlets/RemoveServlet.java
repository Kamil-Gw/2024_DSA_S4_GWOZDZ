package homelibrary.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * Servlet for removing a book from the database
 */
public class RemoveServlet extends HttpServlet {
    /**
     * Default constructor.
     */
    public RemoveServlet() {
        super();
    }

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
        String ownerId = getOwnerId(request);
        String tableHtml = getTableOfPublications(ownerId);
        String errorMessages = (String) request.getAttribute("error-messages");

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter())
        {
            out.println("""
                        <HTML>
                        <HEAD>
                            <TITLE>Home Library &middot; User's Publications</TITLE>
                            <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
                        </HEAD>
                        <BODY>
                            <H1>Home Library &middot; Your Publications:</H1>
                            %s<BR/>
                            <P><A href="browse">Return</A> to browse.</P>
                            <div style="color: red;">%s</div>
                            <DIV>
                                <P>Go back <A href="home">home</A>.</P>
                            </DIV>
                        </BODY>
                        </HTML>
                        """.formatted(tableHtml, errorMessages != null ? errorMessages : ""));
        }
    }

    /**
     * Returns the owner ID of the user who is currently logged in.
     *
     * @param request servlet request
     * @return the owner ID of the user who is currently logged in
     */
    private String getOwnerId(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        return (session != null) ? (String) session.getAttribute("id") : null;
    }

    /**
     * Returns the HTML code of the table of publications of the user with the given owner ID.
     *
     * @param ownerId the owner ID of the user
     * @return the HTML code of the table of publications of the user with the given owner ID
     */
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
                    p.id,
                    p.title,
                    EXTRACT(YEAR FROM p.publication_date) AS "year",
                    p.isbn,
                    p.issn,
                    STRING_AGG(a.name || ' ' || a.surname, ', ') AS authors
                FROM
                    app.publications p
                    JOIN app.authorships pa ON p.id = pa.publication_id
                    JOIN app.authors a ON pa.author_id = a.id
                WHERE
                    p.owner_id = %s
                GROUP BY
                    p.title,
                    p.publication_date,
                    p.isbn,
                    p.issn,
                    p.id;
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
                                             <th>Authors</th>
                                             <th>Action</th>
                                         </tr>
                                     """);
                    do
                    {
                        String title = results.getString("title");
                        String year = results.getString("year");
                        String isbn = results.getString("isbn");
                        String issn = results.getString("issn");
                        String authors = results.getString("authors");
                        String isbnissn = (isbn != null) ? isbn : issn;
                        String id = results.getString("id");
                        tableHtml.append("""
                                         <tr>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td><form action='removing' method='get'>
                                            <input type='hidden' name='id' value="%s">
                                            <input type='submit' value='Remove'>
                                            </form></td>
                                         </tr>
                                         """.formatted(title, year, isbnissn, authors, id));
                    }
                    while (results.next());
                    tableHtml.append("</table>");
                }
                else
                {
                    tableHtml.append("<p>You do not have any books yet.</p>");
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
