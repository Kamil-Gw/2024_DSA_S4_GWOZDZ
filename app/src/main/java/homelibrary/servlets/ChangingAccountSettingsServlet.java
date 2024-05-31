package homelibrary.servlets;

import jakarta.servlet.RequestDispatcher;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kay Jay O'Nail
 */
public class ChangingAccountSettingsServlet extends HttpServlet
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
        HttpSession session = request.getSession(false);
        String userId = (session != null) ? (String) session.getAttribute("id") : null;

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        List<String> errors = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement();
             PrintWriter out = response.getWriter())
        {
            out.println("<HTML><BODY>");

            /* ----- Proof whether the password was correct ----- */
            String password = request.getParameter("current-password");
            String select = String.format("""
                    SELECT
                            u."id" AS "id",
                            u."password" AS "password"
                    FROM 
                            app."users" u
                    WHERE
                            u."id" = %s
                        AND u."password" = '%s'
                    """, userId, password);
            ResultSet results = statement.executeQuery(select);

            if (results.next())
            {
                out.println("<P>The password is correct.</P>");

                /* ----- Fetch the current values of the data ----- */
                String selectCurrent = String.format("""
                        SELECT
                                u."username" AS "username",
                                u."password" AS "password",
                                u."email" AS "email",
                                u."status" AS "status"
                        FROM
                                app.users u
                        WHERE
                                "id" = %s
                        """, userId);
                ResultSet currentData = statement.executeQuery(selectCurrent);
                currentData.next();
                String currentUsername = currentData.getString("username");
                String currentPassword = currentData.getString("password");
                String currentEmail = currentData.getString("email");
                String currentStatus = currentData.getString("status");

                out.println("<P>Current: username = %s, password = %s, email = %s, status = %s</P>"
                        .formatted(currentUsername, currentPassword, currentEmail, currentStatus));

                /* ----- Fetch the new values of the data -----  */
                String newUsername = request.getParameter("new-username");
                String newPassword1 = request.getParameter("new-password-1");
                String newPassword2 = request.getParameter("new-password-2");
                String newEmail = request.getParameter("new-email");
                String newStatus = request.getParameter("new-status");

                out.println("<P>Proposed: username = %s, password = %s/%s, email = %s, status = %s</P>"
                        .formatted(newUsername, newPassword1, newPassword2, newEmail, newStatus));

                /* ----- Update the username ----- */
                if (!newUsername.equals(currentUsername))
                {
                    out.println("<P>About to update the username...</P>");

                    boolean isAvailable = !proofIfUsernameIsUsed(newUsername);

                    out.println("<P>The proposed username is available!</P>");

                    if (isAvailable)
                    {
                        String updateUsername = String.format("""
                                UPDATE
                                        app.users
                                SET
                                        "username" = '%s'
                                WHERE
                                        "id" = %s
                                """, newUsername, userId);
                        statement.executeUpdate(updateUsername);

                        out.println("<P>The username was updated!</P>");
                    }
                    else
                    {
                        errors.add("Proposed username is already in use.");

                        out.println("<P>Proposed username is already in use.</P>");
                    }
                }

                /* ----- Update the password ----- */
                if (!newPassword1.isEmpty() && !newPassword2.isEmpty())
                {
                    out.println("<P>About to update the password...</P>");

                    if (!newPassword1.equals(currentPassword))
                    {
                        out.println("<P>The proposed password is indeed different than the current.</P>");

                        if (isCorrectPassword(newPassword1))
                        {
                            out.println("<P>The proposed password has correct format.</P>");

                            if (newPassword1.equals(newPassword2))
                            {
                                out.println("<P>The proposed password is correctly confirmed.</P>");

                                String updatePassword = String.format("""
                                        UPDATE
                                                app.users
                                        SET
                                                "password" = '%s'
                                        WHERE
                                                "id" = %s
                                        """, newPassword1, userId);
                                statement.executeUpdate(updatePassword);

                                out.println("<P>The password was updated!</P>");
                            }
                            else
                            {
                                errors.add("Proposed passwords do not match.");

                                out.println("<P>Proposed passwords do not match.</P>");
                            }
                        }
                        else
                        {
                            errors.add("Proposed password is incorrect. (Requirements: at"
                                       + " least 8 characters, at least one of the"
                                       + " following: small letter, big letter, digit,"
                                       + " special character).");

                            out.println("<P>The proposed password has incorrect format.</P>");
                        }
                    }
                }

                /* ----- Update the email ----- */
                if (!newEmail.equals(currentEmail))
                {
                    out.println("<P>About to update the email address.</P>");

                    boolean isAvailable = !proofIfEmailIsUSed(newEmail);

                    out.println("<P>The proposed email address is available.</P>");

                    if (isAvailable)
                    {
                        String updateEmail = String.format("""
                                UPDATE
                                        app.users
                                SET
                                        "email" = '%s'
                                WHERE
                                        "id" = %s
                                """, newEmail, userId);
                        statement.executeUpdate(updateEmail);

                        out.println("<P>The email was updated.</P>");
                    }
                }

                /* ----- Update the status ----- */
                if (!newStatus.equals(currentStatus))
                {
                    out.println("<P>About to update the status...</P>");

                    String updateStatus = String.format("""
                            UPDATE
                                    app.users
                            SET
                                    "status" = '%s'
                            WHERE
                                    "id" = %s
                            """, newStatus, userId);
                    statement.executeUpdate(updateStatus);
                    
                    out.println("<P>The status was updated.</P>");
                }
            }
            out.println("</BODY></HTML>");
        }
        catch (SQLException sql)
        {
            errors.add(sql.getMessage());
        }

        if (errors.isEmpty())
        {
//            RequestDispatcher dispatcher = request.getRequestDispatcher("/home");
//            dispatcher.forward(request, response);
        }
        else
        {
            StringBuilder errorsHtml = new StringBuilder();
            errorsHtml.append("<P>");
            for (var error : errors)
            {
                errorsHtml.append(error).append("<BR/>");
            }
            errorsHtml.append("</P>");

            session.setAttribute("error-messaes", errorsHtml.toString());

//            RequestDispatcher dispatcher = request.getRequestDispatcher("/change-settings");
//            dispatcher.forward(request, response);
        }
    }

    private boolean proofIfUsernameIsUsed(String username) throws SQLException
    {
        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        boolean isInUse;
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            String select = """
                        SELECT
                                u."username" AS "username"
                        FROM
                                app.users u
                        WHERE
                                u."username" = '%s'
                        """.formatted(username);
            ResultSet results = statement.executeQuery(select);
            isInUse = results.next();
        }
        return isInUse;
    }

    private boolean isCorrectPassword(String password)
    {
        boolean correct;
        if (password.length() < 8)
        {
            correct = false;
        }
        else
        {
            boolean lowercase = false;
            boolean uppercase = false;
            boolean digit = false;
            boolean special = false;
            for (int i = 0; i < password.length(); ++i)
            {
                char character = password.charAt(i);
                if (character >= 'a' && character <= 'z')
                {
                    lowercase = true;
                }
                else if (character >= 'A' && character <= 'Z')
                {
                    uppercase = true;
                }
                else if (character >= '0' && character <= '9')
                {
                    digit = true;
                }
                else if (character >= '!' && character <= '~')
                {
                    special = true;
                }
                if (lowercase && uppercase && digit && special)
                {
                    break;
                }
            }
            correct = lowercase && uppercase && digit && special;
        }
        return correct;
    }

    private boolean proofIfEmailIsUSed(String email) throws SQLException
    {
        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        boolean isInUse;
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            String select = """
                        SELECT
                                u."email" AS "email"
                        FROM
                                app.users u
                        WHERE
                                u."email" = '%s'
                        """.formatted(email);
            ResultSet results = statement.executeQuery(select);
            isInUse = results.next();
        }
        return isInUse;
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
