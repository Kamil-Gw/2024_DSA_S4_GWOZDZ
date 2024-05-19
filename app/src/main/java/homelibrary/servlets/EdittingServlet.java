package homelibrary.servlets;

import java.io.IOException;
import java.sql.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Kay Jay O'Nail
 */
public class EdittingServlet extends HttpServlet
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
        response.setContentType("text/html;charset=UTF-8");

        String id = request.getParameter("id");
        String title = request.getParameter("title");
        String date = request.getParameter("publication-date");
        String condition = request.getParameter("condition").toLowerCase().replace(' ', '_');
        System.out.println(condition);
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

        if (goodTitle && goodDate && goodCondition && goodType && goodIsbnIssn && goodAuthors) {
            Author[] oldAuthorsArray;
            Author[] authorsArray = new Author[authorsText.length];
            for (int a = 0; a < authorsText.length; ++a)
            {
                int separator = authorsText[a].lastIndexOf(' ');
                String name = authorsText[a].substring(0, separator);
                String surname = authorsText[a].substring(separator + 1);
                authorsArray[a] = new Author(name, surname);
            }

            try {
                String oldAuthorsString = """
                SELECT y.id, y.name, y.surname
                FROM
                    app.authorships x
                    INNER JOIN app.authors y ON x.author_id = y.id
                WHERE publication_id = %s;
                """.formatted(id);

                String isbnOrIssn = type.equals("book") ? "b" : "s";
                String updateBook  = """
                    UPDATE app.publications
                    SET title = '%s',
                        publication_date = '%s',
                        condition = '%s'::app.book_condition,
                        publication_type = '%s'::app.publication_type,
                        is%sn = '%s'
                    WHERE id = %s;
                    """.formatted(title, date, condition, type, isbnOrIssn, isbnIssn, id);
                System.out.println("------");
                System.out.println(updateBook);

                Driver driver = new org.postgresql.Driver();
                DriverManager.registerDriver(driver);

                String dbUrl = DatabaseConnectionData.DATABASE_URL;
                String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
                String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

                try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                     Statement statement = connection.createStatement())
                {
                    ResultSet authors = statement.executeQuery(oldAuthorsString);
                    System.out.println(authors.getFetchSize() + " authors found.");
                    int columns = authors.getMetaData().getColumnCount();
                    while (authors.next())
                    {
                        for (int i = 1; i <= columns; ++i)
                        {
                            System.out.print(authors.getString(i) + " ");
                        }
                        System.out.println();
                    }
//                    oldAuthorsArray = new Author[authors.getFetchSize()+1];
//                    int i = 0;
//                    while (authors.next())
//                    {
//                        oldAuthorsArray[i] = new Author(authors.getString("name"), authors.getString("surname"));
//                        ++i;
//                    }
//
//                    for (Author author : authorsArray)
//                    {
//                        boolean found = false;
//                        for (Author oldAuthor : oldAuthorsArray)
//                        {
//                            if (author.equals(oldAuthor))
//                            {
//                                found = true;
//                                break;
//                            }
//                        }
//                        if (!found)
//                        {
//                            // check if author is in the database
//                            String checkAuthor = """
//                            SELECT id
//                            FROM app.authors
//                            WHERE name = '%s' AND surname = '%s';
//                            """.formatted(author.getName(), author.getSurname());
//
////                            if no author -> add it
//                            ResultSet authorId = statement.executeQuery(checkAuthor);
//                            if (!authorId.next())
//                            {
//                                String addAuthor = """
//                                INSERT INTO app.authors (name, surname)
//                                VALUES ('%s', '%s');
//                                """.formatted(author.getName(), author.getSurname());
//                                statement.executeUpdate(addAuthor);
//                            }
//
//                            // add authorship
//                            String addAuthorship = """
//                            INSERT INTO app.authorships (publication_id, author_id)
//                            VALUES (%s, (SELECT id FROM app.authors WHERE name = '%s' AND surname = '%s'));
//                            """.formatted(id, author.getName(), author.getSurname());
//                            statement.executeUpdate(addAuthorship);
//                        }
//                    }

                    statement.executeUpdate(updateBook);
                }
            }
            catch (SQLException sql) {
                request.setAttribute("error-messages", sql.toString());
                RequestDispatcher dispatcher = request.getRequestDispatcher("/edit");
                dispatcher.forward(request, response);
            }

//            TODO -> adding, removing authors

            RequestDispatcher dispatcher = request.getRequestDispatcher("/edit");
            dispatcher.forward(request, response);
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
