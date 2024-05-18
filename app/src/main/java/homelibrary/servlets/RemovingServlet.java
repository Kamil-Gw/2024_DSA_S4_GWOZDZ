package homelibrary.servlets;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;

public class RemovingServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String ownerId = getOwnerId(request);
        response.setContentType("text/html;charset=UTF-8");
        Boolean deletion = deleteBook(request);

        RequestDispatcher dispatcher;
        dispatcher = request.getRequestDispatcher("/remove");

        dispatcher.forward(request, response);
    }

    private String getOwnerId(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        return (session != null) ? (String) session.getAttribute("id") : null;
    }

    private Boolean deleteBook(HttpServletRequest request)
    {
        String bookId = request.getParameter("id");
        try {
            Driver driver = new org.postgresql.Driver();
            DriverManager.registerDriver(driver);

            String dbUrl = DatabaseConnectionData.DATABASE_URL;
            String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
            String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

            String queryAuthorship = "DELETE FROM app.authorships WHERE publication_id = ?";
            String queryPublication = "DELETE FROM app.publications WHERE id = ?";

            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
                try {
                    connection.setAutoCommit(false);
                    try (PreparedStatement statementAuthorship = connection.prepareStatement(queryAuthorship)) {
                        statementAuthorship.setLong(1, Long.parseLong(bookId));
                        statementAuthorship.executeUpdate();
                    }
                    try (PreparedStatement statementPublication = connection.prepareStatement(queryPublication)) {
                        statementPublication.setLong(1, Long.parseLong(bookId));
                        statementPublication.executeUpdate();
                    }
                    connection.commit();
                    return true;
                }
                catch (SQLException sql) {
                    connection.rollback();
                    request.setAttribute("error-messages", sql.toString());
                    return false;
                }
            }
        }
        catch (SQLException sql)
        {
            request.setAttribute("error-messages", sql.toString());
            return false;
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