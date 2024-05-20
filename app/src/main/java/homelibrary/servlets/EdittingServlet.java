package homelibrary.servlets;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Kay Jay O'Nail
 */
public class EdittingServlet extends DSAServlet {
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String bookId = request.getParameter("id");
        String title = request.getParameter("title");
        String date = request.getParameter("publication-date");
        String condition = request.getParameter("condition").toLowerCase().replace(' ', '_');
        String type = request.getParameter("publication-type");
        String isbnIssn = request.getParameter("isbn/issn");
        String[] authorsText = request.getParameter("authors").split("; ");

//        ArrayList<Long> authorIds = new ArrayList<>();

        System.out.println(authorsText.length);
        for (String author : authorsText) {
            System.out.println(author);
        }

        /* Proof whether they are correct. */
        boolean goodTitle = !title.isBlank();
        boolean goodDate = !date.isBlank();
        boolean goodCondition = !condition.isEmpty();
        boolean goodType = !type.isEmpty();
        boolean goodIsbnIssn = isbnIssn.length() >= 13 && isbnIssn.length() <= 16;
        boolean goodAuthors = !(authorsText.length == 1 && authorsText[0].isBlank());

        if (goodTitle && goodDate && goodCondition && goodType && goodIsbnIssn && goodAuthors) {

            try {
                String isbnOrIssn = type.equals("book") ? "b" : "s";
                String updateBook = updateBook(title, date, condition, type, isbnIssn, bookId);
                String deleteOldAuthorships = deleteOldAuthorships(bookId);

//                ------------------------------------------------
//                MAIN FLOW

                executeUpdate(updateBook);
                executeUpdate(deleteOldAuthorships);

                for (String author : authorsText) {
                    String[] nameAndSurname = parseNameAndSurname(author);
                    long authorId = returnAuthorId(nameAndSurname[0], nameAndSurname[1]);
                    addAuthorship(bookId, authorId);
                }

//                ------------------------------------------------
            } catch (SQLException sql) {
                request.setAttribute("error-messages", sql.toString());
                RequestDispatcher dispatcher = request.getRequestDispatcher("/edit");
                dispatcher.forward(request, response);
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher("/edit");
            dispatcher.forward(request, response);
        }
    }

    private void addAuthorship(String bookId, long authorId) {
        String query = """
                INSERT INTO app.authorships (publication_id, author_id)
                VALUES (%s, %s);
                """.formatted(bookId, authorId);

        try {
            executeUpdate(query);
        } catch (SQLException sql) {
            System.out.println(sql);
        }
    }

    private String updateBook(String title, String date, String condition, String type, String isbnIssn, String id) {
        String isbnOrIssn = type.equals("book") ? "b" : "s";

        return """
                UPDATE app.publications
                SET title = '%s',
                    publication_date = '%s',
                    condition = '%s'::app.book_condition,
                    publication_type = '%s'::app.publication_type,
                    is%sn = '%s'
                WHERE id = %s;
                """.formatted(title, date, condition, type, isbnOrIssn, isbnIssn, id);
    }

    private String deleteOldAuthorships(String id) {
        return """
                DELETE FROM app.authorships
                WHERE publication_id = %s;
                """.formatted(id);
    }

    private String[] parseNameAndSurname(String author) {
        String[] nameAndSurname = author.split(" ");
        String name = nameAndSurname[0];
        String surname = nameAndSurname[1];
        return new String[]{name, surname};
    }

    private long returnAuthorId(String name, String surname) {
        String query = """
                SELECT id
                FROM app.authors
                WHERE name = '%s' AND surname = '%s';
                """.formatted(name, surname);

        try {
            ResultSet rs = executeQuery(query);
//            check if the author exists
            if (rs.next()) {
                return rs.getLong("id");
            } else {
                String insertAuthor = """
                        INSERT INTO app.authors (name, surname)
                        VALUES ('%s', '%s')
                        RETURNING id;
                        """.formatted(name, surname);

                rs = executeQuery(insertAuthor);
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException sql) {
            System.out.println(sql);
        }

        return -1;
    }

}
