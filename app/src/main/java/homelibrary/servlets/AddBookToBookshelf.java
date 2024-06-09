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
 * Servlet for adding a book to a bookshelf.
 */
public class AddBookToBookshelf extends HttpServlet {

    /**
     * Processes requests for both HTTP GET and POST methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String nextServlet = request.getParameter("previousServlet");
        if(nextServlet != null)
        {
            nextServlet = "bookshelf";
        }
        else {
            nextServlet = "home";
        }

        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("<title>Select and Inputs</title>");
        out.println("<style>");
        out.println("    body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }");
        out.println("    .container { width: 90%; margin: 10px auto; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }");
        out.println("    .form-section { padding: 20px; background-color: #f9f9f9; border: 1px solid #ccc; border-radius: 5px; margin-bottom: 20px; }");
        out.println("    label { font-weight: bold; margin-bottom: 5px; display: block; }");
        out.println("    select, input[type=\"text\"] { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 5px; margin-bottom: 10px; box-sizing: border-box; }");
        out.println("    select:focus, input[type=\"text\"]:focus { outline: none; border-color: #2980b9; }");
        out.println("    .button-container { display: flex; gap: 10px; }");
        out.println("    .submit-button { background-color: #3498db; color: #fff; border: none; padding: 10px 20px; text-align: center; text-decoration: none; display: inline-block; font-size: 16px; border-radius: 5px; cursor: pointer; transition: background-color 0.3s; }");
        out.println("    .submit-button:hover { background-color: #2980b9; }");
        out.println("    .back-button { background-color: #e74c3c; color: #fff; border: none; padding: 10px 20px; text-align: center; text-decoration: none; display: inline-block; font-size: 16px; border-radius: 5px; cursor: pointer; transition: background-color 0.3s; }");
        out.println("    .back-button:hover { background-color: #c0392b; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<H1>Home Library &middot; Add Book to Bookshelf:</H1>");
        out.println("<div class=\"container\">");
        out.println("    <form method=\"post\">");
        out.println("        <div class=\"form-section\">");
        out.println("            <label for=\"shelf-select\">Select a Shelf:</label>");
        out.println("            <select id=\"shelf-select\" name=\"shelf\">");
        out.println(getShelvesNames(Long.parseLong(getOwnerId(request))));
        out.println("            </select>");
        out.println("        </div>");
        out.println("        <div class=\"form-section\">");
        out.println("            <label for=\"book-select\">Select a Book:</label>");
        out.println("            <select id=\"book-select\" name=\"book\">");
        out.println(getBookTitles(Long.parseLong(getOwnerId(request))));
        out.println("            </select>");
        out.println("        </div>");
        out.println("        <div class=\"button-container\">");
        out.println("            <button type=\"submit\" class=\"submit-button\">Add Book</button>");
        out.println("            <button type=\"button\" class=\"back-button\" onclick=\"goBack();\">Back</button>");
        out.println("        </div>");
        out.println("    </form>");
        out.println("</div>");
        out.println("</body>");
        out.println("<script>");
        out.println("function goBack() {");
        out.println("window.location.href = '%s';".formatted(nextServlet));
        out.println("}");
        out.println("</script>");
        out.println("</html>");
    }

    /**
     * Get the owner ID from the session.
     */
    private String getOwnerId(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        return (session != null) ? (String) session.getAttribute("id") : null;
    }

    /**
     * Get the names of the shelves from the database.
     */
    private String getShelvesNames(Long ownerId)
    {
        StringBuilder tableHtml = new StringBuilder();
        try {
            Driver driver = new org.postgresql.Driver();
            DriverManager.registerDriver(driver);

            String dbUrl = DatabaseConnectionData.DATABASE_URL;
            String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
            String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

            String queryShelves = "Select app.bookshelves.name from app.bookshelves where"
                    + " app.bookshelves.owner_id = ?";

            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 PreparedStatement preparedStatement = connection.prepareStatement(queryShelves))
            {
                preparedStatement.setLong(1, ownerId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next())
                {
                    tableHtml.append("""
                            <option value=%s>%s</option>""".formatted(resultSet.getString("name"),
                            resultSet.getString("name")));
                }
            }
        }
        catch (SQLException sql)
        {
            System.out.println(sql);
        }
        return tableHtml.toString();
    }

    /**
     * Get the titles of the books from the database.
     */
    private String getBookTitles(Long ownerId)
    {
        StringBuilder tableHtml = new StringBuilder();
        try {
            Driver driver = new org.postgresql.Driver();
            DriverManager.registerDriver(driver);

            String dbUrl = DatabaseConnectionData.DATABASE_URL;
            String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
            String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

            String queryShelves = "Select app.publications.title from app.publications where"
                    + " app.publications.owner_id = ?";

            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 PreparedStatement preparedStatement = connection.prepareStatement(queryShelves))
            {
                preparedStatement.setLong(1, ownerId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next())
                {
                    tableHtml.append("""
                            <option value=%s>%s</option>""".formatted(resultSet.getString("title"),
                            resultSet.getString("title")));
                }
            }
        }
        catch (SQLException sql)
        {
            System.out.println(sql);
        }
        return tableHtml.toString();
    }

    /**
     * Add a book to a bookshelf in the database.
     */
    private boolean addBookBookshelfToDatabase(String shelfName, String title, String ownerId)
    {
        try {
            String dbUrl = DatabaseConnectionData.DATABASE_URL;
            String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
            String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

            String queryShelfId = "SELECT id FROM app.bookshelves WHERE owner_id = ? AND name = ?";
            String queryUpdate = "UPDATE app.publications SET shelf_id = ? WHERE owner_id = ? AND title = ?";

            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 PreparedStatement getShelfIdStatement = connection.prepareStatement(queryShelfId);
                 PreparedStatement preparedStatement = connection.prepareStatement(queryUpdate)) {

                long parsedOwnerId = Long.parseLong(ownerId);

                getShelfIdStatement.setLong(1, parsedOwnerId);
                getShelfIdStatement.setString(2, shelfName);
                ResultSet resultShelfId = getShelfIdStatement.executeQuery();

                if (resultShelfId.next()) {
                    long shelfId = resultShelfId.getLong("id");
                    preparedStatement.setLong(1, shelfId);
                    preparedStatement.setLong(2, parsedOwnerId);
                    preparedStatement.setString(3, title);
                    int rowsAffected = preparedStatement.executeUpdate();
                    System.out.println(rowsAffected + " rows updated successfully.");
                } else {
                    System.out.println(shelfName);
                    System.out.println("Shelf not found.");
                    return false;
                }
            }
        } catch (SQLException sql) {
            System.out.println(sql); // Better error handling would be preferred
            return false;
        }
        return true;
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
        String title = request.getParameter("book");
        String shelf = request.getParameter("shelf");
        addBookBookshelfToDatabase(shelf, title, getOwnerId(request));
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
