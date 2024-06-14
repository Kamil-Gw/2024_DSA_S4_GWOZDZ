package homelibrary.servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The servlet executing addition of a book, in response to the site printed by AddServlet.
 * 
 */
public class AddingServlet extends HttpServlet
{
    /**
     * Default constructor.
     */
    public AddingServlet()
    {
        super();
    }

    /**
     * Adds a book to the database. In case of success, redirects to BrowseServlet.
     * Otherwise, adds errors as request attribute and redirects back to AddServlet.
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

        /* Retrieve the data from the form. */
        String title = request.getParameter("title");
        String date = request.getParameter("publication-date");
        String condition = request.getParameter("condition");
        String type = request.getParameter("publication-type");
        String isbnIssn = request.getParameter("isbn/issn");
        String[] authorsText = request.getParameter("authors").split("; ");

        /* Proof whether they are correct. */
        boolean goodTitle = !title.isBlank();
        boolean goodDate = !date.isBlank();
        boolean goodCondition = !condition.isEmpty();
        boolean goodType = !type.isEmpty();
        boolean goodIsbnIssn = isbnIssn.length() >= 13 && isbnIssn.length() <= 16;
        boolean goodAuthors = !(authorsText.length == 1 && authorsText[0].isBlank());

        if (goodTitle && goodDate && goodCondition && goodType && goodIsbnIssn && goodAuthors)
        {
            /* Transform the authors from textual form into objects. */
            Author[] authorsArray = new Author[authorsText.length];
            for (int a = 0; a < authorsText.length; ++a)
            {
                int separator = authorsText[a].lastIndexOf(' ');
                String name = authorsText[a].substring(0, separator);
                String surname = authorsText[a].substring(separator + 1);
                authorsArray[a] = new Author(name, surname);
            }

            try
            {
                /* --- Do the adding. --- */
                HttpSession session = request.getSession(false);
                String ownerId = (session != null) ? (String) session.getAttribute("id") : null;

                /* Add the publication. */
                Long publicationId = addPublication(title, date, condition, type, isbnIssn, ownerId);
                Set<Long> authorIds = getAuthorIds(authorsArray);
                addAuthorships(publicationId, authorIds);
            }
            catch (SQLException sql)
            {
                request.setAttribute("error-messages", sql.toString());
                RequestDispatcher dispatcher = request.getRequestDispatcher("/add");
                dispatcher.forward(request, response);
            }

            /* --- Dispatch to BrowseServlet. --- */
            RequestDispatcher dispatcher = request.getRequestDispatcher("/browse");
            dispatcher.forward(request, response);
        }
        else
        {
            StringBuilder errors = new StringBuilder();
            if (!goodTitle)
            {
                errors.append("The title shall be provided.;");
            }
            if (!goodDate)
            {
                errors.append("The date shall be provided.;");
            }
            if (!goodCondition)
            {
                errors.append("The condition shall be provided.;");
            }
            if (!goodType)
            {
                errors.append("The type shall be provided;");
            }
            if (!goodIsbnIssn)
            {
                errors.append("The ISBN/ISSN shall be provided.;");
            }
            if (!goodAuthors)
            {
                errors.append("The authors shall be provided;");
            }
            request.setAttribute("error-messages", errors.toString());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/add");
            dispatcher.forward(request, response);
        }
    }

    /**
     * Adds the publication to the database and returns its ID.
     * 
     * @param title title of the publication
     * @param date publication date of the publication
     * @param condition condition of the publication
     * @param type type of the publication
     * @param isbnIssn ISBN or ISSN
     * @param ownerId the owner's ID
     * @return ID of the newly added publication
     * @throws SQLException if an SQL error occurs
     */
    private Long addPublication(String title, String date, String condition,
                                   String type, String isbnIssn, String ownerId) throws SQLException
    {
        String isbnOrIssn = type.equals("book") ? "isbn" : "issn";
        String insert = """
                        INSERT INTO
                                app.publications
                                (
                                    "title",
                                    "owner_id",
                                    "publication_date",
                                    "condition",
                                    "publication_type",
                                    "%s"
                                )
                        VALUES
                                (
                                    '%s',
                                    %s,
                                    '%s',
                                    '%s',
                                    '%s',
                                    '%s'
                                )
                        """.formatted(isbnOrIssn,
                title,
                ownerId,
                date,
                condition,
                type,
                isbnIssn);
        
        String select = """
                        SELECT
                                p.id
                        FROM
                                app.publications p
                        WHERE
                                "title"='%s'
                            AND "owner_id"=%s
                            AND "publication_date"='%s'
                            AND "condition"='%s'
                            AND "publication_type"='%s'
                            AND "%s"='%s'
                        """.formatted(title, ownerId, date, condition, type,
                isbnOrIssn, isbnIssn);
        
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        Long result = null;
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            statement.executeUpdate(insert);
            ResultSet results = statement.executeQuery(select);
            if (results.next())
            {
                result = results.getLong("id");
            }
        }

        return result;
    }
    
    /**
     * Fetches IDs of the authors.
     * 
     * @param authors array of the authors to find the IDs
     * @return list of IDs
     * @throws SQLException if an SQL error occurs
     */
    private Set<Long> getAuthorIds(Author[] authors) throws SQLException
    {
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        Set<Long> ids = new HashSet<>(authors.length);
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            for (var author : authors)
            {
                String select = """
                    SELECT
                            a.id
                    FROM
                            app.authors a
                    WHERE
                            "name"='%s'
                        AND "surname"='%s'
                    """.formatted(author.name, author.surname);
                ResultSet results = statement.executeQuery(select);
                if (results.next())
                {
                    Long id = results.getLong("id");
                    ids.add(id);
                }
                else
                {
                    String insert = """
                        INSERT INTO
                                app.authors
                                (
                                    "name",
                                    "surname"
                                )
                        VALUES
                                (
                                    '%s',
                                    '%s'
                                )
                        """.formatted(author.name, author.surname);
                    statement.executeUpdate(insert);
                    results = statement.executeQuery(select);
                    if (results.next())
                    {
                        Long id = results.getLong("id");
                        ids.add(id);
                    }
                }
            }
        }
        return ids;
    }

    /**
     * Adds authorship records to the database. Each author from given set is recorded of
     * given publication.
     * 
     * @param publicationId ID of the publciation
     * @param authorIds IDs of the authors
     * @throws SQLException if an SQL error occurs
     */
    private void addAuthorships(Long publicationId, Set<Long> authorIds) throws SQLException
    {
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            for (var authorId : authorIds)
            {
                String insert = """
                                INSERT INTO
                                        app.authorships
                                        (
                                            author_id,
                                            publication_id
                                        )
                                VALUES
                                        (
                                            %d,
                                            %d
                                        )
                                """.formatted(authorId, publicationId);
                statement.executeUpdate(insert);
            }
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
