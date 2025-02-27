import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class ExportCSVServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        String tableName = request.getParameter("table_name");
        System.out.println("The tableName is " + tableName);
        if (tableName == null || tableName.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Table name is required");
            return;
        }
        
        tableName = tableName.toLowerCase();
        System.out.println(tableName);

        String columnQuery = "SELECT column_name FROM information_schema.columns WHERE table_name = ? AND table_schema = 'public' ORDER BY ordinal_position";
        String dataQuery = "SELECT * FROM " + tableName;

        String csvFilePath = "S:\\Zoho\\CSV_files\\" + tableName + ".csv";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement prep = con.prepareStatement(columnQuery);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(dataQuery);
             BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFilePath))) {
            
            prep.setString(1, tableName);
            ResultSet columnResult = prep.executeQuery();

            List<String> columnNames = new ArrayList<>();
            while (columnResult.next()) {
                columnNames.add(columnResult.getString("column_name"));
            }

            csvWriter.write(String.join(",", columnNames));
            csvWriter.newLine();

           
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA, 10);

            float margin = 20;
            float yStart = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yPosition = yStart;
            float rowHeight = 20;
            float cellMargin = 5;
            int numColumns = columnNames.size();
            float[] colWidths = new float[numColumns];
            Arrays.fill(colWidths, tableWidth / numColumns);

          
            float xPosition = margin;
            contentStream.setLineWidth(0.5f);
            yPosition -= rowHeight;

            for (String columnName : columnNames) {
                contentStream.beginText();
                contentStream.newLineAtOffset(xPosition + cellMargin, yPosition + 5);
                contentStream.showText(columnName);
                contentStream.endText();
                contentStream.addRect(xPosition, yPosition, colWidths[0], rowHeight);
                contentStream.stroke();
                xPosition += colWidths[0];
            }

            while (rs.next()) {
                xPosition = margin;
                yPosition -= rowHeight;
                
                List<String> row = new ArrayList<>();
                for (String columnName : columnNames) {
                    Object value = rs.getObject(columnName);
                    String cellValue = value == null ? "" : value.toString().replace("\"", "\"\"");
                    row.add(cellValue);
                }
                csvWriter.write(String.join(",", row));
                csvWriter.newLine();

                for (String cell : row) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition + cellMargin, yPosition + 5);
                    contentStream.showText(cell);
                    contentStream.endText();

                    contentStream.addRect(xPosition, yPosition, colWidths[0], rowHeight);
                    contentStream.stroke();

                    xPosition += colWidths[0];
                }

                if (yPosition < margin + rowHeight) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    yPosition = yStart;
                }
            }

            contentStream.close();

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + tableName + ".pdf\"");
        
            document.save(response.getOutputStream());
            document.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL Error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "File Writing Error: " + e.getMessage());
        }
    }
}