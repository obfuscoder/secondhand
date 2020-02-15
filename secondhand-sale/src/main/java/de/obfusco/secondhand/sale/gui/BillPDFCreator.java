package de.obfusco.secondhand.sale.gui;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import de.obfusco.secondhand.storage.model.BaseItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillPDFCreator {

    private static NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    private static PdfPTable insertItemTable(List<BaseItem> data, BigDecimal sum) {

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
            BaseItem item = data.get(i);
            table.addCell(Integer.toString(i + 1));
            cell = new PdfPCell(new Phrase(item.code));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(item.getCategoryName()));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(item.getSize()));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(CURRENCY.format(item.price)));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            //new Row
            cell = new PdfPCell(new Phrase(item.description));
            cell.setColspan(5);
            table.addCell(cell);
        }

        cell = new PdfPCell(new Phrase(new Chunk("Summe", FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(CURRENCY.format(sum), FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        cell.setColspan(4);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        return table;
    }

    public File createPdf(Path basePath, List<BaseItem> data, BigDecimal sum)
            throws IOException, DocumentException {

        Files.createDirectories(basePath);
        Path targetPath = Paths.get(basePath.toString(), "sale.pdf");

        Document document = new Document();// new Rectangle(250, 1000));
        PdfWriter.getInstance(document,
                new FileOutputStream(targetPath.toFile()));
        document.open();

        String date = SimpleDateFormat.getDateInstance(0, Locale.GERMAN).format(new Date());

        document.add(new Phrase(new Chunk("Rechnung Flohmarkt ",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24))));
        document.add(new Paragraph(""));
        document.add(new Phrase(new Chunk(date, FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        document.add(new Paragraph("\n"));

        document.add(insertItemTable(data, sum));

        document.add(new Paragraph("\n"));
        document.add(new Phrase(new Chunk("Vielen Dank für Ihren Einkauf.",
                FontFactory.getFont(FontFactory.HELVETICA, 10))));

        document.close();

        return targetPath.toFile();
    }
}
