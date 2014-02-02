package de.obfusco.secondhand.payoff.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import de.obfusco.secondhand.storage.model.Event;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.repository.ReservationRepository;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TotalPayOff {

    String path = "C:\\flohmarkt\\Abrechnung\\";
    String completeBillPath = "C:\\flohmarkt\\KomplettAbrechnung\\";
    String completionpath = "C:\\flohmarkt\\completion\\";
    String soldpath = "C:\\flohmarkt\\Sold\\";
    String completesoldpath = "C:\\flohmarkt\\CompleteSold\\";
    String soldfile = "sold.csv";
    String completesoldfile = "completesold.csv";

    boolean completePayoff = false;

    private static final float ENTRY_FEE = 2.50f;

    @Autowired
    ReservedItemRepository reservedItemRepository;

    @Autowired
    ReservationRepository reservationRepository;

    public File createTotalPayoffFile(Event event) throws DocumentException, FileNotFoundException {

        (new File(path)).mkdirs();

        String fullPath = path + "total.pdf";
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fullPath));
        document.open();

        document.add(new Phrase(new Chunk("Abrechnung Flohmarkt ", FontFactory
                .getFont(FontFactory.HELVETICA_BOLD, 24))));

        document.add(createTotalTable(event));

        document.close();

        return new File(fullPath);
    }

    public PdfPTable createTotalTable(Event event) {
        List<Reservation> reservations
                = reservationRepository.findByEvent(event);

        List<ReservedItem> soldItems
                = reservedItemRepository.findByReservationEventAndSoldNotNull(event);

        PdfPTable table = new PdfPTable(2);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        PdfPCell cell;

        float sum = 0;
        for (ReservedItem item : soldItems) {
            sum += item.getItem().getPrice().floatValue();
        }

        float kitaSum = sum * 0.2f;
        float totalEntryFees = ENTRY_FEE * reservations.size();
        float totalSum = kitaSum + totalEntryFees;

        cell = new PdfPCell(new Phrase(new Chunk("Anzahl verkaufter Artikel",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(Integer.toString(soldItems.size()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk("Summe verkaufter Artikel",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(sum + " EURO",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk("Anteil Kitas (20%)",
                FontFactory.getFont(FontFactory.HELVETICA, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(kitaSum + " EURO",
                FontFactory.getFont(FontFactory.HELVETICA, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk("Teilnahmegebühren für "
                + reservations.size() + " Teilnehmer", FontFactory.getFont(
                        FontFactory.HELVETICA, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(totalEntryFees + " EURO",
                FontFactory.getFont(FontFactory.HELVETICA, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk("Gewinn insgesamt",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(totalSum + " EURO",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk("Gewinn je Kita",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(totalSum / 2 + " EURO",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        return table;
    }
}
