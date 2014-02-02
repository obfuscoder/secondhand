package de.obfusco.secondhand.payoff.file;

import java.text.NumberFormat;
import java.util.Locale;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

abstract public class BasePayOff {

    protected static final double CHILDCARE_SHARE = 0.2;
    protected static final double ENTRY_FEE = 2.5;

    protected NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    protected NumberFormat percent = NumberFormat.getPercentInstance(Locale.GERMANY);

    protected void addTotalLine(PdfPTable table, String label, String value, boolean bold) {
        Font font = FontFactory.getFont((bold) ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, 12);
        PdfPCell cell;
        cell = new PdfPCell(new Phrase(new Chunk(label, font)));
        cell.setColspan(4);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(value, font)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }

    protected void addHeader(Document document) throws DocumentException {
        document.add(new Phrase(new Chunk("Abrechnung Flohmarkt", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24))));
    }
}
