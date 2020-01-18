package de.obfusco.secondhand.payoff.file;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import de.obfusco.secondhand.storage.model.Event;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

abstract class BasePayOff {

    NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    NumberFormat percent = NumberFormat.getPercentInstance(Locale.GERMANY);

    void addTotalLine(PdfPTable table, String label, boolean bold, int size, int[] alignments, String... values) {
        Font font = FontFactory.getFont((bold) ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, size);
        PdfPCell cell;
        cell = new PdfPCell(new Phrase(new Chunk(label, font)));
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        cell.setBorderWidth(1);
        cell.setColspan(table.getNumberOfColumns() - values.length);
        if (alignments != null) cell.setHorizontalAlignment(alignments[0]);
        table.addCell(cell);
        int column = 0;
        for (String value: values) {
            cell = new PdfPCell(new Phrase(new Chunk(value, font)));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM | Rectangle.LEFT);
            cell.setBorderWidth(1);
            if (alignments != null) cell.setHorizontalAlignment(alignments[column + 1]);
            table.addCell(cell);
            column += 1;
        }
    }

    void addHeader(Document document, Event event) throws DocumentException {
        document.add(new Phrase("Abrechnung Flohmarkt", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(new Phrase("\n" + event.name + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        document.add(new Phrase(
                SimpleDateFormat.getDateInstance(0, Locale.GERMAN).format(new Date()),
                FontFactory.getFont(FontFactory.HELVETICA, 10)));
    }
}
