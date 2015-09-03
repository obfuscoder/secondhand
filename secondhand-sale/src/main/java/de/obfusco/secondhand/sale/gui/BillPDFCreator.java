package de.obfusco.secondhand.sale.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

import de.obfusco.secondhand.storage.model.Item;

public class BillPDFCreator {

    private static NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    public File createPdf(Path basePath, List<Item> data)
            throws IOException, DocumentException {

        Files.createDirectories(basePath);
        Path targetPath = Paths.get(basePath.toString(), "sale.pdf");

        Document document = new Document();// new Rectangle(250, 1000));
        PdfWriter.getInstance(document,
                new FileOutputStream(targetPath.toFile()));
        document.open();

        Calendar cal = Calendar.getInstance();
        String date = cal.get(Calendar.DATE) + "." + cal.get(Calendar.MONTH + 1)
                + "." + cal.get(Calendar.YEAR) + " "
                + cal.get(Calendar.HOUR_OF_DAY) + ":"
                + cal.get(Calendar.MINUTE);

        document.add(new Phrase(new Chunk("Rechnung Flohmarkt ",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24))));
        document.add(new Paragraph(""));
        document.add(new Phrase(new Chunk(date, FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        document.add(new Paragraph("\n"));

        document.add(insertItemTable(data));

        document.add(new Paragraph("\n"));
        document.add(new Phrase(new Chunk("Vielen Dank für Ihren Einkauf.",
                FontFactory.getFont(FontFactory.HELVETICA, 10))));

        document.close();

        return targetPath.toFile();
    }

    public static PdfPTable insertItemTable(List<Item> data) {

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

        double sum = 0.0;
        for (int i = 0; i < data.size(); i++) {
            Item item = data.get(i);
            table.addCell(Integer.toString(i + 1));
            cell = new PdfPCell(new Phrase(item.code));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(item.category.name));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(item.size));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(currency.format(item.price)));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            //new Row
            cell = new PdfPCell(new Phrase(item.description));
            cell.setColspan(5);
            table.addCell(cell);

            sum += item.price.doubleValue();
        }

        cell = new PdfPCell(new Phrase("------------------------------------------------------------"));
        cell.setColspan(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk("Summe", FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(currency.format(sum), FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        cell.setColspan(4);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        return table;
    }
}
