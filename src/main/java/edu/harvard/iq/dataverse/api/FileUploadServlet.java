package edu.harvard.iq.dataverse.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

// from https://github.com/javaee-samples/javaee7-samples/blob/master/servlet/file-upload/src/main/java/org/javaee7/servlet/file/upload/TestServlet.java
/**
 * @todo Is a whole new servlet really necessary? See also
 * http://stackoverflow.com/questions/2422468/how-to-upload-files-to-server-using-jsp-servlet/2424824#2424824
 */
@WebServlet(urlPatterns = {"/uploadServlet"})
@MultipartConfig(location = "/tmp")
public class FileUploadServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>File Upload Servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>File Upload Servlet</h1>");
            out.println("Receiving the uploaded file ...<br>");
            out.println("Received " + request.getParts().size() + " parts ...<br>");
            String fileName = "";
            for (Part part : request.getParts()) {
                out.println("... name: " + part.getName());
                out.println("... size: " + part.getSize());
                out.println("... header names: " + part.getHeaderNames());
                out.println("... content type: " + part.getContentType());
                String content = new BufferedReader(new InputStreamReader(part.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));
                out.println("... content: " + content);
                fileName = part.getSubmittedFileName();
                out.println("... writing " + fileName + " part<br>");
                if (fileName != null) {
                    part.write(fileName);
                }
                out.println("... written<br>");
                out.println("---<br>");
            }
            out.println("... uploaded to: /tmp/" + fileName);
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
