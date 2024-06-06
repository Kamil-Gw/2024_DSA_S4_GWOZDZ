package homelibrary.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Enumeration type representing type of publication.
 * 
 * @author Kay Jay O'Nail
 */
enum PublicationType
{
    /**
     * Book.
     */
    BOOK,
    
    /**
     * Journal.
     */
    JOURNAL;
}

/**
 * Servlet responsible for preparing the site with form for adding a book.
 * 
 * @author Kay Jay O'Nail
 */
public class AddServlet extends HttpServlet
{
    /**
     * Writes the HMTL site to the response. The site contains a form for adding a book.
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

        try (PrintWriter out = response.getWriter())
        {
            out.println("""
            <!DOCTYPE html>
            <HTML>
                <HEAD>
                    <TITLE>Home Library &middot; Adding a Publication</TITLE>
                    <META charset="UTF-8">
                    <META name="viewport" content="width=device-width, initial-scale=1.0">
                    <STYLE>
                        .Wide
                        {
                            width: 300px;
                        }
                        .Narrow
                        {
                            width: 150px;
                        }
                        .Textarea
                        {
                            font-family: "Arial";
                        }
                    </STYLE>
                    <SCRIPT>
                        var list = [];
                        function addAuthor()
                        {
                            // The textarea for showing the output values.
                            var outputField = document.getElementById('output');

                            // The custom input.
                            const customInput1 = document.getElementById('custom1');
                            const customInput2 = document.getElementById('custom2');
                            const customValue1 = customInput1.value.trim();
                            const customValue2 = customInput2.value.trim();
                            const customValue = customValue1.concat(' ', customValue2);
                            if (customValue1 !== '' && customValue2 !== '' && !list.includes(customValue))
                            {
                                if (outputField.value === '')
                                {
                                    outputField.value = customValue;
                                }
                                else
                                {
                                    outputField.value = outputField.value.concat('; ', customValue);
                                }
                                list.push(customValue);
                            }
                            customInput1.value = '';
                            customInput2.value = '';

                            // The dropdown list.
                            const selectionList = document.getElementById('selection');
                            const selectionValue = selectionList.value;

                            if (selectionValue !== '' && !list.includes(selectionValue))
                            {
                                if (outputField.value === '')
                                {
                                    outputField.value = selectionValue;
                                }
                                else
                                {
                                    outputField.value = outputField.value.concat('; ', selectionValue);
                                }
                                list.push(selectionValue);
                            }
                            selectionList.value = '';
                    }
                    </SCRIPT>
                </HEAD>
                <BODY>
                    <H1>Home Library &middot; Add a Publication</H1>
                    <DIV>
                        %s
                        <FORM action="adding" method="post">
                            <TABLE>
                                <TR>
                                    <TH align="left">Title:</TH>
                                    <TD>
                                        <INPUT name="title" type="text" class="Wide"/>
                                    </TD>
                                </TR>
                                <TR>
                                    <TH align="left">Date of publication:</TH>
                                    <TD>
                                        <INPUT name="publication-date" type="date" class="Wide"/>
                                    </TD>
                                </TR>
                                <TR>
                                    <TH align="left">Condition:</TH>
                                    <TD>
                                        <SELECT name="condition" class="Wide">
                                            <OPTION value="">Select</OPTION>
                                            <OPTION value="like_new">Like new</OPTION>
                                            <OPTION value="excellent">Excellent</OPTION>
                                            <OPTION value="very_good">Very good</OPTION>
                                            <OPTION value="good">Good</OPTION>
                                            <OPTION value="acceptable">Acceptable</OPTION>
                                            <OPTION value="poor">Poor</OPTION>
                                        </SELECT>
                                    </TD>
                                </TR>
                                <TR>
                                    <TH align="left">Publication type:</TH>
                                    <TD>
                                        <SELECT name="publication-type" class="Wide">
                                            <OPTION value="">Select</OPTION>
                                            <OPTION value="book">Book</OPTION>
                                            <OPTION value="journal">Journal</OPTION>
                                        </SELECT>
                                    </TD>
                                </TR>
                                <TR>
                                    <TH align="left">ISBN/ISSN:</TH>
                                    <TD>
                                        <INPUT name="isbn/issn" type="text" class="Wide"/>
                                    </TD>
                                </TR>
                                <TR>
                                    <TH align="left">Authors:</TH>
                                    <TD>
                                        <TEXTAREA id="output" name="authors" readonly class="Wide Textarea" style="Wide Textarea"></TEXTAREA>
                                    </TD>
                                </TR>
                                    <TR>
                                        <TH rowspan="2" align="right">
                                            <BUTTON type="button" onClick="addAuthor()">Add<BR/>the author</BUTTON>
                                        </TH>
                                        <TD>
                                            <INPUT id="custom1" type="text" placeholder="Enter Name" class="Narrow"/>
                                            <INPUT id="custom2" type="text" placeholder="Enter Surname" class="Narrow"/>
                                        </TD>
                                    </TR>
            """.formatted(extractErrors(request)));

            try
            {
                var authors = getAuthors();
                if (!authors.isEmpty())
                {
                    out.println("""
                                <TR>
                                    <TD>
                                        <SELECT id="selection" class="Wide">
                                            <OPTION value="">Select Author</OPTION>
                    """);
                    out.println(authorsAsOptionsHtml(authors));
                    out.println("""
                                        </SELECT>
                                    </TD>
                                </TR>
                    """);
                }
            }
            catch (SQLException sql)
            {
                out.println(sql.getMessage());
            }

            out.println("""
                            </TABLE><BR/>
                            <BUTTON type="submit">Add the publication<BR/>to your collection</BUTTON>
                        </FORM>
                    </DIV>
                    <DIV>
                        <P>Go back <A href="home">home</A>.</P>
                    </DIV>
                </BODY>
            </HTML>
            """);
        }
    }

    /**
     * Fetches authors from the database.
     * 
     * @return list of authors
     * @throws SQLException if an SQL error occurs
     */
    private List<Author> getAuthors() throws SQLException
    {
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;

        List<Author> authors = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            String query = """
                           SELECT
                                   name, surname
                           FROM
                                   app.authors
                           """;
            ResultSet results = statement.executeQuery(query);
            while (results.next())
            {
                String name = results.getString("name");
                String surname = results.getString("surname");
                Author author = new Author(name, surname);

                authors.add(author);
            }
        }
        return authors;
    }

    /**
     * Generates a piece of HTML code that contains the authors as options for a SELECT
     * element.
     * 
     * @param authors list of authors
     * @return the HTML code
     */
    private String authorsAsOptionsHtml(List<Author> authors)
    {
        StringBuilder optionsHtml = new StringBuilder();
        for (var author : authors)
        {
            optionsHtml.append("""
                <OPTION value="%1$s %2$s">%1$s %2$s</OPTION>
                """.formatted(author.name, author.surname));
        }
        return optionsHtml.toString();
    }

    /**
     * Proofs whether the previous servlet has set attribute with errors. If yes, extracts
     * the errors.
     * 
     * @param request the HTTP request that might contain information about errors
     * @return information about errors in form of HTML paragraph, or an empty string
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
