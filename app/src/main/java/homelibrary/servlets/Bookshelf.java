package homelibrary.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.*;

public class Bookshelf extends HttpServlet {

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
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String htmlResponse = "<!DOCTYPE html>"
                + "<html lang=\"en\">"
                + "<head>"
                + "    <meta charset=\"UTF-8\">"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "    <title>Bookshelf</title>"
                + "    <style>"
                + "        body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }"
                + "        .bookshelf { width: 90%; margin: 10px auto; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }"
                + "        .shelf {padding: 10px; background-color: #f9f9f9; border: 1px solid #ccc; border-radius: 5px; }"
                + "        .shelf-name { font-weight: bold; margin-bottom: 10px; }"
                + "        .books { display: flex; flex-wrap: wrap; justify-content: start; margin-bottom: 10px; padding: 10px;}"
                + "        .book { width: 50px; height: 100px; background-color: #8e44ad; color: #fff; display: flex; align-items: center; justify-content: center; text-align: center; padding: 10px; box-sizing: border-box; border-radius: 5px; margin: 5px; }"
                + "        .book:hover { background-color: #732d91; }"
                + "        .title { font-size: 14px; font-weight: bold; }"
                + "        .author { font-size: 12px; }"
                + "        .add-shelf-button { background-color: #3498db; color: #fff; border: none; padding: 10px 20px; text-align: center; text-decoration: none; display: inline-block; font-size: 16px; border-radius: 5px; cursor: pointer; transition: background-color 0.3s; }"
                + "        .add-shelf-button:hover { background-color: #2980b9; }"
                + "    </style>"
                + "</head>"
                + "<body>"
                + "    <div class=\"bookshelf\">"
                + getShelves(Long.parseLong(getOwnerId(request)))
                + "        <button id=\"addShelfButton\" class=\"add-shelf-button\" onclick=\"addShelf()\">Add Shelf</button>"
                + "    </div>"
                + "</body>"
                + "<script>\n" +
                "    function addShelf() {\n" +
                "        var shelfName = prompt('Enter shelf name:');\n" +
                "        if (shelfName) {\n" +
                "            var xhr = new XMLHttpRequest();\n" +
                "            xhr.open(\"POST\", \"bookshelf\", true);\n" +
                "            xhr.setRequestHeader(\"Content-Type\", \"application/x-www-form-urlencoded\");\n" +
                "            xhr.onreadystatechange = function () {\n" +
                "                if (xhr.readyState === 4 && xhr.status === 200) {\n" +
                "                    var newShelf = document.createElement('div');\n" +
                "                    newShelf.className = 'shelf';\n" +
                "                    var shelfNameElement = document.createElement('div');\n" +
                "                    shelfNameElement.className = 'shelf-name';\n" +
                "                    shelfNameElement.textContent = shelfName;\n" +
                "                    newShelf.appendChild(shelfNameElement);\n" +
                "                    var booksDiv = document.createElement('div');\n" +
                "                    booksDiv.className = 'books';\n" +
                "                    newShelf.appendChild(booksDiv);\n" +
                "                    var bookshelf = document.querySelector('.bookshelf');\n" +
                "                    bookshelf.insertBefore(newShelf, document.getElementById('addShelfButton'));\n" +
                "                }\n" +
                "            };\n" +
                "            xhr.send(\"action=addShelf&shelfName=\" + encodeURIComponent(shelfName));\n" +
                "        }\n" +
                "    }\n" +
                "</script>"
                + "</html>";
        response.getWriter().write(htmlResponse);
    }

    private String getOwnerId(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        return (session != null) ? (String) session.getAttribute("id") : null;
    }

    private String getShelves(Long ownerId)
    {
        StringBuilder tableHtml = new StringBuilder();
        try {
            Driver driver = new org.postgresql.Driver();
            DriverManager.registerDriver(driver);

            String dbUrl = DatabaseConnectionData.DATABASE_URL;
            String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
            String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

            String queryShelves = "Select app.bookshelves.name, app.bookshelves.id from app.bookshelves where"
                    + " app.bookshelves.owner_id = ?";
            String queryBooks = "Select app.publications.title from app.publications where "
                    + "app.publications.owner_id = ? and app.publications.shelf_id = ?";

            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 PreparedStatement preparedStatement = connection.prepareStatement(queryShelves))
            {
                preparedStatement.setLong(1, ownerId);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next())
                {
                    tableHtml.append("""
                        <div class="shelf">
                        <div class="shelf-name">%s</div>
                        <div class="books">
                """.formatted(resultSet.getString("name")));

                    try (PreparedStatement bookStmt = connection.prepareStatement(queryBooks)) {
                        bookStmt.setLong(1, ownerId);
                        bookStmt.setLong(2, resultSet.getLong("id"));
                        ResultSet resultBooks = bookStmt.executeQuery();
                        while(resultBooks.next())
                        {
                            tableHtml.append("""
                                    <div class="book">
                                    <div>
                                    <div class="title">%s</div>
                                    </div>
                                    </div>
                                    """.formatted(resultBooks.getString("title")));
                        }
                    }
                    tableHtml.append("""
                                        </div>
                                        </div>
                                        """);
                }
            }
        }
        catch (SQLException sql)
        {
            System.out.println(sql.toString());
        }
        return tableHtml.toString();
    }

    private void handleAddShelf(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String shelfName = request.getParameter("shelfName");

        if (shelfName != null && !shelfName.isEmpty()) {
            boolean success = addShelfToDatabase(shelfName, getOwnerId(request), request);

            if (success) {
                response.getWriter().write("Shelf added successfully!");
            } else {
                response.getWriter().write("Failed to add shelf to the database.");
            }
        } else {
            response.getWriter().write("Shelf name cannot be empty.");
        }
    }

    private boolean addShelfToDatabase(String shelfName, String owner_id, HttpServletRequest request) {
        try {
            Driver driver = new org.postgresql.Driver();
            DriverManager.registerDriver(driver);

            String dbUrl = DatabaseConnectionData.DATABASE_URL;
            String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
            String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

            String query = "Insert into app.bookshelves (name, description, owner_id, location) values (?, ?, ?, ?)";

            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 PreparedStatement preparedStatement = connection.prepareStatement(query))
            {
                preparedStatement.setString(1, shelfName);
                preparedStatement.setString(2, " ");
                preparedStatement.setLong(3, Long.parseLong(owner_id));
                preparedStatement.setString(4, " ");
                preparedStatement.executeUpdate();
            }
        }
        catch (SQLException sql)
        {
            System.out.println(sql.toString());
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
        String action = request.getParameter("action");

        if ("addShelf".equals(action)) {
            handleAddShelf(request, response);
        }
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
