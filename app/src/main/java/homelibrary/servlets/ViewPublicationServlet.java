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

class PublicationData
{
    String title;
    String date;
    String type;
    String condition;
    String isbn;
    String issn;
    String authors;
    String owner;
}

/**
 *
 * @author Kay Jay O'Nail 
 */
public class ViewPublicationServlet extends HttpServlet
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
        String publicationId = request.getParameter("id");
        PublicationData data = null;
        String error = "";
        try
        {
            data = getPublicationData(publicationId);
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
                            <TITLE>Home Library &middot; View Publication</TITLE>
                        </HEAD>
                        <BODY>
                            <H1>Home Library &middot; View Publication</H1>
                        """);
            
            if (data != null)
            {
                out.println("""
                            <H2>Data of the publication</H2>
                            <TABLE border="1">
                                <TR>
                                    <TH>Title:</TH>
                                    <TD>%1$s</TD>
                                </TR>
                                <TR>
                                    <TH>Authors:</TH>
                                    <TD>%2$s</TD>
                                </TR>
                                <TR>
                                    <TH>Type:</TH>
                                    <TD>%3$s</TD>
                                </TR>
                                <TR>
                                    <TH>Date of publication:</TH>
                                    <TD>%4$s</TD>
                                </TR>
                                <TR>
                                    <TH>%5$s:</TH>
                                    <TD>%6$s</TD>
                                </TR>
                                <TR>
                                    <TH>Condition:</TH>
                                    <TD>%7$s</TD>
                                </TR>
                                <TR>
                                    <TH>Owner:</TH>
                                    <TD>%8$s</TD>
                                </TR>
                            </TABLE>
                            <H2>Reservation Request</H2>
                            <P>You want to reserve this book? Send a reservation request!</P>
                            <FORM action="reserve" method="get">
                                <INPUT name="publication-id" type="hidden" value="%9$s"/>
                                <TABLE border="1">
                                    <TR>
                                        <TH>
                                            Since when?
                                        </TH>
                                        <TD>
                                            <INPUT name="date-since" type="date"/>
                                        </TD>
                                    </TR>
                                    <TR>
                                        <TH>
                                            Until when?
                                        </TH>
                                        <TD>
                                            <INPUT name="date-until" type="date"/>
                                        </TD>
                                    </TR>
                                </TABLE><BR/>
                                <BUTTON type="submit">Request</BUTTON>
                            </FORM>
                            """
                            .formatted(
                                data.title, // 1$
                                data.authors, // 2$
                                data.type, // 3$
                                data.date, // 4$
                                (data.isbn != null) ? "ISBN" : "ISSN", // 5$
                                (data.isbn != null) ? data.isbn : data.issn, // 6$
                                data.condition, // 7$
                                data.owner, // 8$
                                publicationId // 9$
                            )
                );
            }
            else
            {
                out.println("<P>%s</P>".formatted(error));
            }
            
            out.println("""
                        </BODY>
                        </HTML>
                        """);
            
        }
    }
    
    private PublicationData getPublicationData(String id) throws SQLException
    {
        String select = """
                        SELECT
                                p."title" AS "title",
                                STRING_AGG(a."name" || ' ' || a."surname", '; ') AS "authors",
                                p."publication_type" AS "type",
                                p."publication_date" AS "date",
                                p."isbn" AS "isbn",
                                p."issn" AS "issn",
                                p."condition" AS "condition",
                                u."username" AS "owner"
                        FROM
                                app.publications p
                            JOIN
                                app.authorships pa ON p."id" = pa."publication_id"
                            JOIN
                                app.authors a ON pa."author_id" = a."id"
                            JOIN
                                app.users u ON u."id" = p."owner_id"
                        WHERE
                                p."id" = %s
                        GROUP BY
                                p."id",
                                p."title",
                                u."id",
                                u."username"
                        """.formatted(id);
        
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;
        
        PublicationData data = null;
        
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            ResultSet results = statement.executeQuery(select);
            if (results.next())
            {
                data = new PublicationData();
                data.title = results.getString("title");
                data.authors = results.getString("authors");
                data.type = results.getString("type");
                data.date = results.getString("date");
                data.isbn = results.getString("isbn");
                data.issn = results.getString("issn");
                data.condition = results.getString("condition");
                data.owner = results.getString("owner");
            }
        }
        
        return data;
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
