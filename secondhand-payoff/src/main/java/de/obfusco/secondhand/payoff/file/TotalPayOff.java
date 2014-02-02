package de.obfusco.secondhand.payoff.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
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
public class TotalPayOff extends BasePayOff {

    @Autowired
    ReservedItemRepository reservedItemRepository;

    @Autowired
    ReservationRepository reservationRepository;

    public File createTotalPayoffFile(String path, Event event) throws DocumentException, FileNotFoundException {

        String fullPath = path + "total.pdf";
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fullPath));
        document.open();
        addHeader(document);
        document.add(createTotalTable(event));
        document.close();
        return new File(fullPath);
    }

    private PdfPTable createTotalTable(Event event) {
        List<Reservation> reservations
                = reservationRepository.findByEvent(event);
        List<ReservedItem> soldItems
                = reservedItemRepository.findByReservationEventAndSoldNotNull(event);

        PdfPTable table = new PdfPTable(5);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        PdfPCell cell;

        double sum = 0;
        for (ReservedItem item : soldItems) {
            sum += item.getItem().getPrice().doubleValue();
        }

        double kitaSum = sum * CHILDCARE_SHARE;
        double totalEntryFees = ENTRY_FEE * reservations.size();
        double totalSum = kitaSum + totalEntryFees;

        addTotalLine(table, "Anzahl verkaufter Artikel", Integer.toString(soldItems.size()), true);
        addTotalLine(table, "Summe verkaufter Artikel", currency.format(sum), true);
        addTotalLine(table, "Anteil Kitas (" + percent.format(CHILDCARE_SHARE) + ")", currency.format(kitaSum), false);
        addTotalLine(table, "Teilnahmegebühren für " + reservations.size() + " Teilnehmer", currency.format(totalEntryFees), false);
        addTotalLine(table, "Gewinn insgesamt", currency.format(totalSum), true);
        addTotalLine(table, "Gewinn je Kita", currency.format(totalSum / 2), true);

        return table;
    }
}
