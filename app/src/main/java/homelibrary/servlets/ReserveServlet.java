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
 * Servlet for reserving a publication
 */
public class ReserveServlet extends HttpServlet
{
    /**
     * Default constructor.
     */
    public ReserveServlet()
    {
        super();
    }

    /**
     * Represents data for a publication.
     */
    class PublicationData {
        /**
         * The title of the publication.
         */
        String title;

        /**
         * The date of publication.
         */
        String date;

        /**
         * The type of publication (e.g., book, magazine, journal).
         */
        String type;

        /**
         * The condition of the publication (e.g., new, used, good, fair, poor).
         */
        String condition;

        /**
         * The International Standard Book Number (ISBN) of the publication.
         */
        String isbn;

        /**
         * The International Standard Serial Number (ISSN) of the publication.
         */
        String issn;

        /**
         * The authors of the publication.
         */
        String authors;

        /**
         * The owner of the publication.
         */
        String owner;

        /**
         * The unique identifier of the owner.
         */
        int ownerId;

        /**
         * Default constructor.
         */
        public PublicationData()
        {
            this.title = "";
            this.date = "";
            this.type = "";
            this.condition = "";
            this.isbn = "";
            this.issn = "";
            this.authors = "";
            this.owner = "";
            this.ownerId = 0;
        }
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
        response.setContentType("text/html;charset=UTF-8");
        String publicationId = request.getParameter("id");
        PublicationData data = null;
        String error = "";
        
        try
        {
            data = getPublicationData(publicationId);
            error = extractErrors(request);
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
                            <TITLE>Home Library &middot; Reserve</TITLE>
                        </HEAD>
                        <BODY>
                            <H1>Home Library &middot; Reserve</H1>
                        """);

            if (data != null)
            {
                HttpSession session = request.getSession(false);
                String userId = (String) session.getAttribute("id");
                
                if (userId.equals(String.valueOf(data.ownerId)))
                {
                    data.owner = data.owner.concat(" (you)");
                }
                
                if (!error.isEmpty())
                {
                    out.println("""
                                <H2>Errors:</H2>
                                <P>%s</P>
                                """.formatted(error));
                }

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
                            """.formatted(
                                data.title, // 1$
                                data.authors, // 2$
                                data.type, // 3$
                                data.date, // 4$
                                (data.isbn != null) ? "ISBN" : "ISSN", // 5$
                                (data.isbn != null) ? data.isbn : data.issn, // 6$
                                data.condition, // 7$
                                data.owner // 8$
                            )
                );

                if (!userId.equals(String.valueOf(data.ownerId)))
                {
                    out.println("""
                        <P>You want to reserve this book? <A href="reserving?publication-id=%s">Send</A> a reservation request!</P>
                        """.formatted(publicationId));
                }
                else
                {
                    out.println("""
                        <P>(This book is yours. You can't reserve it.)</P>
                        """);
                }
                
                out.println("""
                        <P>Go back <A href="home">home</A>.</P>
                            """);
            }
            else
            {
                out.println("<P>Error: %s</P>".formatted(error));
            }

            out.println("""
                        </BODY>
                        </HTML>
                        """);

        }
    }

    /**
     * Retrieves the data of a publication
     *
     * @param id the ID of the publication
     * @return the data of the publication
     * @throws SQLException if an error occurs while accessing the database
     */
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
                                u."username" AS "owner",
                                u."id" AS "owner_id"
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
                data.ownerId = results.getInt("owner_id");
            }
        }

        return data;
    }

    /**
     * Extracts error messages from the request
     *
     * @param request the request
     * @return the error messages
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
