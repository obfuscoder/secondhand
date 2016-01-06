package de.obfusco.secondhand.payoff.file;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import de.obfusco.secondhand.storage.model.Event;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

abstract public class BasePayOff {

    protected NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    protected NumberFormat percent = NumberFormat.getPercentInstance(Locale.GERMANY);

    protected void addTotalLine(PdfPTable table, String label, String value, boolean bold, int size) {
        Font font = FontFactory.getFont((bold) ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, size);
        PdfPCell cell;
        cell = new PdfPCell(new Phrase(new Chunk(label, font)));
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        cell.setBorderWidth(1);
        cell.setColspan(5);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(value, font)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM | Rectangle.LEFT);
        cell.setBorderWidth(1);
        table.addCell(cell);
    }

    protected void addHeader(Document document, Event event) throws DocumentException {
        document.add(new Phrase("Abrechnung Flohmarkt", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(new Phrase("\n" + event.name + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        document.add(new Phrase(
                SimpleDateFormat.getDateInstance(0, Locale.GERMAN).format(new Date()),
                FontFactory.getFont(FontFactory.HELVETICA, 10)));
    }
}
