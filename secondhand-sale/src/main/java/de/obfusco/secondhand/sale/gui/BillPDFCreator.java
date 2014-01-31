package de.obfusco.secondhand.sale.gui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class BillPDFCreator {

    public void createPdf(String filename, List<Object> data, String sum, String bar, String change)
            throws IOException, DocumentException {

        Document document = new Document();// new Rectangle(250, 1000));
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(filename));
        document.open();

        Calendar cal = Calendar.getInstance();
        String date = cal.get(Calendar.DATE) + "." + cal.get(Calendar.MONTH + 1)
                + "." + cal.get(Calendar.YEAR) + " "
                + cal.get(Calendar.HOUR_OF_DAY) + ":"
                + cal.get(Calendar.MINUTE);

        document.add(new Phrase(new Chunk("Rechnung Flohmarkt ", FontFactory
                .getFont(FontFactory.HELVETICA_BOLD, 24))));
        document.add(new Paragraph(""));
        document.add(new Phrase(new Chunk(date, FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        document.add(new Paragraph("\n"));

        document.add(insertItemTable(data, sum, bar, change));

        document.add(new Paragraph("\n"));
        document.add(new Phrase(new Chunk("Vielen Dank für Ihren Einkauf. Der Erlös kommt den Kindergärten \n \"Arche Noah\" und \"Regenbogen\" zugute.", FontFactory
                .getFont(FontFactory.HELVETICA, 10))));

        document.close();
    }

    public static PdfPTable insertItemTable(List<Object> data, String sum, String bar, String change) {

        PdfPTable table = new PdfPTable(5);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        PdfPCell cell;

        cell = new PdfPCell(new Phrase(new Chunk("Nr", FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk("ArtNr", FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk("Kategorie",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk("Gr.", FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk("Preis", FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        for (int i = 0; i < data.size(); i++) {
            Object[] items = (Object[]) data.get(i);
            table.addCell(Integer.toString(i + 1));
            cell = new PdfPCell(new Phrase((String) items[0]));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase((String) items[1]));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase((String) items[3]));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase((String) items[4]));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            //new Row
            cell = new PdfPCell(new Phrase((String) items[2]));
            cell.setColspan(5);
            table.addCell(cell);
        }

        cell = new PdfPCell(new Phrase("------------------------------------------------------------"));
        cell.setColspan(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk("Summe", FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(sum + " EURO", FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        cell.setColspan(4);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk("BAR", FontFactory.getFont(
                FontFactory.HELVETICA, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(bar + " EURO", FontFactory.getFont(
                FontFactory.HELVETICA, 12))));
        cell.setColspan(4);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk("Rückgeld", FontFactory.getFont(
                FontFactory.HELVETICA, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(change + " EURO", FontFactory.getFont(
                FontFactory.HELVETICA, 12))));
        cell.setColspan(4);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        return table;
    }
}
