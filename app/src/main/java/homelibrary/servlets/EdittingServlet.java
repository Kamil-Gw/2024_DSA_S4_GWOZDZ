package homelibrary.servlets;

import java.io.IOException;
import java.io.PrintWriter;
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

        if (goodTitle && goodDate && goodCondition && goodType && goodIsbnIssn && goodAuthors) {
            Author[] authorsArray = new Author[authorsText.length];
            for (int a = 0; a < authorsText.length; ++a)
            {
                int separator = authorsText[a].lastIndexOf(' ');
                String name = authorsText[a].substring(0, separator);
                String surname = authorsText[a].substring(separator + 1);
                authorsArray[a] = new Author(name, surname);
            }

            try {
                String isbnOrIssn = type.equals("book") ? "b" : "s";
                String updateBook  = """
                    UPDATE app.publications
                    SET title = '%s',
                        publication_date = '%s'
                        condition = '%s'::app.book_condition,
                        publication_type = '%s'::app.publication_type,
                        is%sn      = '%s'
                    WHERE id = %s;
                    """.formatted(title,
                        date,
                        condition,
                        type,
                        isbnOrIssn,
                        id);

                Driver driver = new org.postgresql.Driver();
                DriverManager.registerDriver(driver);

                String dbUrl = DatabaseConnectionData.DATABASE_URL;
                String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
                String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

                try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                     Statement statement = connection.createStatement())
                {
                    statement.executeUpdate(updateBook);
                }
            }
            catch (SQLException sql) {
                request.setAttribute("error-messages", sql.toString());
                RequestDispatcher dispatcher = request.getRequestDispatcher("/edit");
                dispatcher.forward(request, response);
            }

//            TODO -> adding, removing authors
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
