package homelibrary.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

class SearchingResult
{
    public int id;
    public String title;
    public String authors;
}

/**
 *
 * @author Kay Jay O'Nail
 */
public class SearchServlet extends HttpServlet
{

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
                            <TITLE>Home Library &middot; Search</TITLE>
                        </HEAD>
                        <BODY>
                            <H1>Domowe Librarium &middot; Szukańsko</H1>
                            <P>%s</P>
                            <P>%s</P>
                        </BODY>
                        </HTML>
                        """.formatted(error, table));
        }
    }
    
    private List<SearchingResult> search(String particle) throws SQLException
    {
        List<SearchingResult> list = new ArrayList<>();
        String select = """
                        SELECT
                                p."id" AS "id",
                                p."title" AS "title",
                                STRING_AGG(a."name" || ' ' || a."surname", '; ') AS "authors"
                        FROM
                                app.publications p
                            JOIN
                                app.authorships pa ON p."id" = pa."publication_id"
                            JOIN
                                app.authors a ON pa."author_id" = a."id"
                        WHERE
                                UPPER(p."title") LIKE UPPER('%u%')
                            OR  UPPER(a."name") LIKE UPPER('%u%')
                            OR  UPPER(a."surname") LIKE UPPER('%u%')
                        GROUP BY
                                p."id"
                        """;
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
                list.add(result);
            }
        }
        return list;
    }
    
    private String generateTableHtml(List<SearchingResult> data)
    {
        StringBuilder code = new StringBuilder("""
            <TABLE border="1">
            <TR>
                <TH>Title</TH><TH>Authors</TH><TH>Link to the book</TH>
            </TR>
            """);
        for (var row : data)
        {
            int id = row.id;
            String title = row.title;
            String authors = row.authors;
            code.append("""
                        <TR>
                            <TD>%s</TD>
                            <TD>%s</TD>
                            <TD><A href="view-publication?id=%s">Look</A></TD>
                        </TR>
                        """.formatted(title, authors, id));
        }
        code.append("</TABLE>");
        return code.toString();
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
