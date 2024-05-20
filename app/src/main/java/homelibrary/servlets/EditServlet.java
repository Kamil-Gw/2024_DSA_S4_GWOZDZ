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

class BookData
{
    public int id;
    public String title;
    public String date;
    public String condition;
    public String type;
    public String isbnIssn;
    public String authors;
}

/**
 *
 * @author Kay Jay O'Nail
 */
public class EditServlet extends HttpServlet
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
        
        StringBuilder errors = new StringBuilder(extractErrors(request));
        String switchCases = "", selectOptions = "", authorsOptions = "";
        try
        {
            var books = getBookData();
            switchCases = generateJSSwitchCases(books);
            selectOptions = generateHtmlSelectOptions(books);
            authorsOptions = generateAuthorOptionsHtml();
        }
        catch (SQLException sql)
        {
            errors.append("<P>").append(sql.toString()).append("</P>");
        }
        
        try (PrintWriter out = response.getWriter())
        {
            out.println("""
                        <HTML>
                        <HEAD>
                            <SCRIPT>
                                var list = [];
                                function writeList()
                                {
                                    const authorsTextarea = document.getElementById('new-authors');
                                    if (list.length > 0)
                                    {
                                        authorsTextarea.value = list[0];
                                        for (var i = 1; i < list.length; ++i)
                                        {
                                            // Insert the author into the textarea.
                                            authorsTextarea.value = authorsTextarea.value.concat('; ', list[i]);
                                        }
                                    }
                                    else
                                    {
                                        authorsTextarea.value = '';
                                    }
                                }
                                function flushAuthors()
                                {
                                    const removalSelection = document.getElementById('removal-selection');
                                    const length = removalSelection.options.length;
                                    for (var i = length - 1; i > 0; --i)
                                    {
                                        removalSelection.options[i] = null;
                                    }
                                }
                                function fill()
                                {
                                    // Selection of the publication for edition.
                                    const selection = document.getElementById('main-selection');

                                    // Elements of the form.
                                    const titleInput = document.getElementById('new-title');
                                    const dateInput = document.getElementById('new-date');
                                    const conditionInput = document.getElementById('new-condition');
                                    const typeInput = document.getElementById('new-type');
                                    const isbnIssnInput = document.getElementById('new-isbn/issn');
                                    const authorsTextarea = document.getElementById('new-authors');

                                    const selectedId = String(selection.value);
                                    let correct = true;

                                    switch (selectedId)
                                    {

                                    <!-- DYNAMICALLY GENERATED: -->
                                    %1$s
                                    <!-- /DYNAMICALLY GENERATED -->

                                    default:
                                            correct = false;
                                    }
                                    if (correct)
                                    {
                                        writeList();
                                        flushAuthors();
                                        const removalSelection = document.getElementById('removal-selection');
                                        for (var i = 0; i < list.length; ++i)
                                        {
                                            // Insert the author into the removal selection list.
                                            var option = document.createElement('option');
                                            option.value = list[i];
                                            option.innerHTML = list[i];
                                            removalSelection.appendChild(option);
                                        }
                                    }
                                }
                                function addAuthor()
                                {
                                    // The textarea for showing the output values.
                                    const outputField = document.getElementById('new-authors');

                                    // The custom input.
                                    const customInput1 = document.getElementById('new-name');
                                    const customInput2 = document.getElementById('new-surname');
                                    const customValue1 = String(customInput1.value).trim();
                                    const customValue2 = String(customInput2.value).trim();
                                    const customValue = customValue1.concat(' ', customValue2);
                                    if (customValue1 !== '' && customValue2 !== '' && !list.includes(customValue))
                                    {
                                        // Append to the textarea.
                                        if (outputField.value === '')
                                        {
                                            outputField.value = customValue;
                                        }
                                        else
                                        {
                                            outputField.value = outputField.value.concat('; ', customValue);
                                        }

                                        // Add to the list.
                                        list.push(customValue);

                                        // Add to the removal selection.
                                        const option = document.createElement('option');
                                        option.value = customValue;
                                        option.innerHTML = customValue;
                                        const removalSelection = document.getElementById('removal-selection');
                                        removalSelection.appendChild(option);
                                    }
                                    customInput1.value = '';
                                    customInput2.value = '';

                                    // The dropdown list.
                                    const selectionList = document.getElementById('addition-selection');
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

                                        // Add to the removal selection.
                                        const option = document.createElement('option');
                                        option.value = selectionValue;
                                        option.innerHTML = selectionValue;
                                        const removalSelection = document.getElementById('removal-selection');
                                        removalSelection.appendChild(option);
                                    }
                                    selectionList.value = '';
                                }
                                function removeAuthor()
                                {
                                    const removalSelection = document.getElementById('removal-selection');
                                    const valueForRemoval = removalSelection.value;
                                    const index = list.indexOf(removalSelection.value);
                                    if (index > -1)
                                    {
                                        list.splice(index, 1);
                                        writeList();

                                        const options = removalSelection.options;
                                        const length = options.length;
                                        for (var i = 0; i < length; ++i)
                                        {
                                            if (options[i].value === valueForRemoval)
                                            {
                                                removalSelection.remove(i);
                                            }
                                        }	
                                    }
                                }
                            </SCRIPT>
                        </HEAD>
                        <BODY>
                            <H1>Home Library &middot; Edit a Publication</H1>
                            %3$s
                            <FORM action="editting" method="post">
                                <TABLE border="1" style="background-color: #FAFAFF;">
                                    <TR>
                                        <TD colspan="2" align="center">

                                            <SELECT id="main-selection" name="id">
                                                <OPTION value=""> -- Select -- </OPTION>

                                                <!-- DYNAMICALLY GENERATED: -->
                                                %2$s
                                                <!-- /DYNAMICALLY GENERATED -->

                                            </SELECT>
                                            <BUTTON type="button" onClick="fill()">Fill</BUTTON>

                                        </TD>
                                    </TR>
                                    <TR>
                                        <TH>New title:</TH>
                                        <TD>

                                            <INPUT id="new-title" name="title" type="text"/>

                                        </TD>
                                    </TR>
                                    <TR>
                                        <TH>New date of publication:</TH>
                                        <TD>

                                            <INPUT id="new-date" name="publication-date" type="date"/>

                                        </TD>
                                    </TR>
                                    <TR>
                                        <TH>New condition:</TH>
                                        <TD>

                                            <SELECT id="new-condition" name="condition">
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

                                        <TH>New publication type:</TH>
                                        <TD>
                                            <SELECT id="new-type" name="publication-type">
                                                <OPTION value="">Select</OPTION>
                                                <OPTION value="book">Book</OPTION>
                                                <OPTION value="journal">Journal</OPTION>
                                            </SELECT>
                                        </TD>

                                    </TR>
                                    <TR>
                                        <TH>New ISBN/ISSN:</TH>
                                        <TD>

                                            <INPUT id="new-isbn/issn" name="isbn/issn" type="text"/>

                                        </TD>
                                    </TR>
                                    <TR>

                                        <TH>New Authors:</TH>
                                        <TD>
                                            <TEXTAREA id="new-authors" name="authors" style="width: 300px; height: 100px; font-family: 'Arial';" readonly></TEXTAREA><BR/>

                                            <SELECT id="removal-selection">
                                                <OPTION value="">Select author to remove</OPTION>
                                            </SELECT>

                                            <BUTTON type="button" onClick="removeAuthor()">Remove</BUTTON><BR/>

                                            <SELECT id="addition-selection">
                                                <OPTION value="">Select author to add</OPTION>
                        
                                                <!-- DYNAMICALLY GENERATED: -->
                                                %4$s
                                                <!-- /DYNAMICALLY GENERATED -->
                        
                                            </SELECT><BR/>

                                            <INPUT id="new-name" placeholder="Name to add"/>
                                            <INPUT id="new-surname" placeholder="Surname to add"/><BR/>

                                            <BUTTON type="button" onClick="addAuthor()">Add</BUTTON>

                                        </TD>
                                    </TR>
                                </TABLE><BR/>
                                <BUTTON type="submit">Edit</BUTTON>
                            </FORM>
                            <DIV>
                                <P>Go back <A href="home">home</A>.</P>
                            </DIV>
                        </BODY>
                        </HTML>
                        """.formatted(switchCases, selectOptions, errors.toString(), authorsOptions));
        }
    }
    
    private List<BookData> getBookData() throws SQLException
    {
        List<BookData> list = new ArrayList<>();
        String select = """
                        SELECT
                                p."id" AS "id",
                                p."title" AS "title",
                                p."publication_date" AS "date",
                                p."condition" AS "condition",
                                p."publication_type" AS "type",
                                p."isbn" AS "isbn",
                                p."issn" AS "issn",
                                STRING_AGG(a."name" || ' ' || a."surname", '; ') AS "authors"
                        FROM
                                app.publications p
                            JOIN
                                app.authorships pa ON p."id" = pa."publication_id"
                            JOIN
                                app.authors a ON pa."author_id" = a."id"
                        GROUP BY
                        	p."id"
                        """;
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;
        
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            ResultSet results = statement.executeQuery(select);
            while (results.next())
            {
                BookData book = new BookData();
                book.id = results.getInt("id");
                book.title = results.getString("title");
                book.date = results.getString("date");
                book.condition = results.getString("condition");
                book.type = results.getString("type");
                String isbn = results.getString("isbn");
                String issn = results.getString("issn");
                book.isbnIssn = (isbn != null) ? isbn : issn;
                book.authors = results.getString("authors");
                list.add(book);
            }
        }
        return list;
    }
    
    private String generateJSSwitchCases(List<BookData> books)
    {
        StringBuilder code = new StringBuilder();
        
        for (var book : books)
        {
            String[] authorsArray = book.authors.split("; ");
            StringBuilder authors = new StringBuilder("'%s'".formatted(authorsArray[0]));
            for (int i = 1; i < authorsArray.length; ++i)
            {
                authors.append(", '").append(authorsArray[i]).append("'");
            }
            code.append("""
                        case '%1$d':
                            titleInput.value = '%2$s';
                            dateInput.value = '%3$s';
                            conditionInput.value = '%4$s';
                            typeInput.value = '%5$s';
                            isbnIssnInput.value = '%6$s';
                            list = [%7$s];
                            break;
                        """
                        .formatted(
                            book.id,
                            book.title,
                            book.date,
                            book.condition,
                            book.type,
                            book.isbnIssn,
                            authors.toString()
                        )
            );
        }
        
        return code.toString();
    }
    
    private String generateHtmlSelectOptions(List<BookData> books)
    {
        StringBuilder code = new StringBuilder();
        for (var book : books)
        {
            code.append("""
                        <OPTION value="%1$s">%2$s</OPTION>
                        """.formatted(book.id, book.title));
        }
        return code.toString();
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
        
        return errorsHtml.toString();
    }
    
    private String generateAuthorOptionsHtml() throws SQLException
    {
        Driver driver = new org.postgresql.Driver();
        DriverManager.registerDriver(driver);

        String dbUrl = DatabaseConnectionData.DATABASE_URL;
        String dbUsername = DatabaseConnectionData.DATABASE_USERNAME;
        String dbPassword = DatabaseConnectionData.DATABASE_PASSWORD;
        
        StringBuilder options = new StringBuilder();
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
             Statement statement = connection.createStatement())
        {
            String select = """
                            SELECT
                                    a."name" AS "name",
                                    a."surname" AS "surname"
                            FROM
                                    app.authors a
                            """;
            ResultSet results = statement.executeQuery(select);
            while (results.next())
            {
                String name = results.getString("name");
                String surname = results.getString("surname");
                options.append("""
                               <OPTION value="%1$s %2$s">%1$s %2$s</OPTION>
                               """.formatted(name, surname));
            }
        }
        
        return options.toString();
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
