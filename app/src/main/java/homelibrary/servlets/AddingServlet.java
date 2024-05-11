package homelibrary.servlets;

import jakarta.servlet.RequestDispatcher;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author Kay Jay O'Nail
 */
public class AddingServlet extends HttpServlet
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
        
        String title = request.getParameter("title");
        String date = request.getParameter("publication-date");
        String condition = request.getParameter("condition");
        String type = request.getParameter("publication-type");
        String isbnIssn = request.getParameter("isbn/issn");
        String[] authorsText = request.getParameter("authors").split("; ");
        
        boolean goodTitle = !title.isBlank();
        boolean goodDate = !date.isBlank();
        boolean goodCondition = !condition.isEmpty();
        boolean goodType = !type.isEmpty();
        boolean goodIsbnIssn = !isbnIssn.isEmpty();
        boolean goodAuthors = !(authorsText.length == 1 && authorsText[0].isBlank());
        
        if (goodTitle && goodDate && goodCondition && goodType && goodIsbnIssn && goodAuthors)
        {
            Author[] authorsArray = new Author[authorsText.length];
            for (int a = 0; a < authorsText.length; ++a)
            {
                int separator = authorsText[a].lastIndexOf(' ');
                String name = authorsText[a].substring(0, separator);
                String surname = authorsText[a].substring(separator + 1);
                authorsArray[a] = new Author(name, surname);
            }

            try (PrintWriter out = response.getWriter())
            {
                /* --- Do the adding. --- */
                
                out.println("<!DOCTYPE html>");
                out.println("<HTML>");
                out.println("<HEAD>");
                out.println("<TITLE>Home Library &middot; Adding in Progress</TITLE>");
                out.println("</HEAD>");
                out.println("<BODY>");

                out.println(String.format("""
                            <P>
                            title: %1$s<BR/>
                            date: %2$s<BR/>
                            condition: %3$s<BR/>
                            type: %4$s<BR/>
                            is?n: %5$s<BR/>
                            authors:
                            """,
                            title,
                            date,
                            condition,
                            type,
                            isbnIssn));
                for (var author : authorsArray)
                {
                    out.println("[%s:%s] ".formatted(author.name, author.surname));
                }
                out.println("<BR/>");
                HttpSession session = request.getSession(false);
                String ownerId = (session != null) ? (String) session.getAttribute("id") : null;
                out.println(ownerId);
                out.println("</P>");
                out.println("</BODY>");
                out.println("</HTML>");
                
                /* --- Dispatch to BrowseServlet. --- */
            }
        }
        else
        {
            StringBuilder errors = new StringBuilder();
            if (!goodTitle)
            {
                errors.append("The title shall be provided.;");
            }
            if (!goodDate)
            {
                errors.append("The date shall be provided.;");
            }
            if (!goodCondition)
            {
                errors.append("The condition shall be provided.;");
            }
            if (!goodType)
            {
                errors.append("The type shall be provided;");
            }
            if (!goodIsbnIssn)
            {
                errors.append("The ISBN/ISSN shall be provided.;");
            }
            if (!goodAuthors)
            {
                errors.append("The authors shall be provided;");
            }
            request.setAttribute("error-messages", errors.toString());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/add");
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