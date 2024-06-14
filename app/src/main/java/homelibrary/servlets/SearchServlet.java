package homelibrary.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Searching results class.
 */
class SearchingResult {
    /**
     * Publication id
     */
    public int id;

    /**
     * Publication title
     */
    public String title;

    /**
     * Authors of the publication
     */
    public String authors;

    /**
     * Owner of the publication
     */
    public String owner;

    /**
     * Default constructor.
     */
    public SearchingResult() {
        this.id = 0;
        this.title = "";
        this.authors = "";
        this.owner = "";
    }
}


/**
 * Servlet for searching publications.
 */
public class SearchServlet extends HttpServlet
{
    /**
     * Default constructor.
     */
    public SearchServlet() {
        super();
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        String phrase = request.getParameter("search");
        List<SearchingResult> results;
        String table = "";
        String error = "";
        try
        {
            results = search(phrase);
            table = generateTableHtml(results);
        }
        catch (SQLException sql)
        {
            error = sql.toString();
        }

        try (PrintWriter out = response.getWriter())
        {
            out.println("""
                        <HTML>
                        <HEAD>
                            <TITLE>Home Library &middot; Reservation</TITLE>
                        </HEAD>
                        <BODY>
                            <H1>Home Library &middot; Reservation</H1>
                            <P>%s</P>
                            <P>%s</P>
                            <DIV>
                                <P>Go back <A href="home">home</A>.</P>
                            </DIV>
                        </BODY>
                        </HTML>
                        """.formatted(error, table));
        }
    }

    /**
     * Searches for publications in the database.
     *
     * @param particle the phrase to search for
     * @return a list of searching results
     * @throws SQLException if an SQL error occurs
     */
    private List<SearchingResult> search(String particle) throws SQLException
    {
        List<SearchingResult> list = new ArrayList<>();
        String select = """
                        SELECT
                                p."id" AS "id",
                                p."title" AS "title",
                                u."username" AS "owner",
                                STRING_AGG(a."name" || ' ' || a."surname", '; ') AS "authors"
                        FROM
                                app.publications p
                            JOIN
                                app.authorships pa ON p."id" = pa."publication_id"
                            JOIN
                                app.authors a ON pa."author_id" = a."id"
                            JOIN
                                app.users u ON u."id" = p."owner_id"
                        WHERE
                                UPPER(p."title") LIKE UPPER('%%%1$s%%')
                            OR  UPPER(a."name") LIKE UPPER('%%%1$s%%')
                            OR  UPPER(a."surname") LIKE UPPER('%%%1$s%%')
                        GROUP BY
                                p."id",
                                u."username"
                        """.formatted(particle);
        
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            ResultSet results = statement.executeQuery(select);
            while (results.next())
            {
                SearchingResult result = new SearchingResult();
                result.id = results.getInt("id");
                result.title = results.getString("title");
                result.authors = results.getString("authors");
                result.owner = results.getString("owner");
                list.add(result);
            }
        }
        return list;
    }

    /**
     * Generates HTML code for a table with searching results.
     *
     * @param data the list of searching results
     * @return the HTML code
     */
    private String generateTableHtml(List<SearchingResult> data)
    {
        if (!data.isEmpty())
        {
            StringBuilder code = new StringBuilder("""
            <TABLE border="1">
            <TR>
                <TH>Title</TH><TH>Authors</TH><TH>Owner</TH><TH>View</TH>
            </TR>
            """);
            for (var row : data)
            {
                int id = row.id;
                String title = row.title;
                String authors = row.authors;
                String owner = row.owner;
                code.append("""
                        <TR>
                            <TD>%s</TD>
                            <TD>%s</TD>
                            <TD>%s</TD>
                            <TD><A href="reserve?id=%s">Look</A></TD>
                        </TR>
                        """.formatted(title, authors, owner, id));
            }
            code.append("</TABLE>");
            return code.toString();
        }
        else
        {
            return "<P>No results.</P>";
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
