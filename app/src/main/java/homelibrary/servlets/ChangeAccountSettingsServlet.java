package homelibrary.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class Data
{
    String username;
    String emailAddress;
    String status;
}

/**
 *
 * @author Kay Jay O'Nail
 */
public class ChangeAccountSettingsServlet extends HttpServlet
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
        String id = (String) session.getAttribute("id");
        String error = extractErrors(request);
        Data data = null;
        try
        {
            data = getData(id);
        }
        catch (SQLException sql)
        {
            error = error.isEmpty() ? sql.toString() : error.concat("<P>").concat(sql.toString()).concat("</P>");
        }
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter())
        {
            String username = (data != null) ? data.username : "error";
            String emailAddress = (data != null) ? data.emailAddress : "error";
            String status = (data != null) ? data.status : "error";
            
            out.println("""
            <HTML>
                <HEAD>
                    <TITLE>Home Library &middot; Account Settings</TITLE>
                </HEAD>
                <BODY>
                    <H1>Home Library &middot; Account Settings</H1>
                    %4$s
                    <FORM action="changing-settings" method="post">
                        <TABLE border="1">
                            <TR>
                                <TH>Username:</TH>
                                <TD><INPUT name="new-username" type="text" value="%1$s" size="30"/></TD>
                            </TR>
                            <TR>
                                <TH>New password:</TH>
                                <TD>
                                    <INPUT name="new-password-1" type="password" size="30" placeholder="leave blank not to change"/>
                                </TD>
                            </TR>
                            <TR>
                                <TH>Confirm password:</TH>
                                <TD>
                                    <INPUT name="new-password-2" type="password" size="30" placeholder="leave blank not to change"/>
                                </TD>
                            </TR>
                            <TR>
                                <TH>Email address:</TH>
                                <TD>
                                    <INPUT name="new-email" type="text" size="30" value="%2$s"/>
                                </TD>
                            </TR>
                            <TR>
                                <TH>Status:</TH>
                                <TD>
                                    <TEXTAREA name="new-status" style="font-family: Arial;">%3$s</TEXTAREA>
                                </TD>
                            </TR>
                        </TABLE><BR/>
                        <P>Input your current password to authorize the changes:</P>
                        <INPUT type="password" name="current-password"/>
                        <BUTTON type="submit">Save Changes</BUTTON>
                    </FORM>
                    <DIV>
                        <P>Go back <A href="home">home</A>.</P>
                    </DIV>
                </BODY>
            </HTML>
            """.formatted(username, emailAddress, status,
                    !error.isEmpty() ? String.format("<P>%s</P>", error) : "")
            );
        }
    }
    
    private Data getData(String userId) throws SQLException
    {
        Data data = new Data();
        
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;
        
        try(Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            Statement statement = connection.createStatement())
        {
            String select = """
                            SELECT
                                    u."username" AS "username",
                                    u."email" AS "email",
                                    u."status" AS "status"
                            FROM
                                    app.users u
                            WHERE
                                    u."id" = %s
                            """.formatted(userId);
            
            ResultSet results = statement.executeQuery(select);
            
            if (results.next())
            {
                data.username = results.getString("username");
                data.emailAddress = results.getString("email");
                data.status = results.getString("status");
            }
        }
        
        return data;
    }
    
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
        else
        {
            errorsHtml.append("NULL");
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