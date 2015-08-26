package de.obfusco.secondhand.payoff.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import de.obfusco.secondhand.storage.model.Event;
import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.repository.ReservationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TotalPayOff extends BasePayOff {

    @Autowired
    de.obfusco.secondhand.storage.repository.ItemRepository ItemRepository;

    @Autowired
    ReservationRepository reservationRepository;

    public File createTotalPayoffFile(Path basePath, Event event) throws DocumentException, IOException {
        Files.createDirectories(basePath);
        Path fullPath = Paths.get(basePath.toString(), "payoff.pdf");
        Document document = new Document(PageSize.A4, 80, 50, 50, 30);
        PdfWriter.getInstance(document,
                new FileOutputStream(fullPath.toFile()));
        document.open();
        addHeader(document, event);
        document.add(createTotalTable(event));
        document.close();
        return fullPath.toFile();
    }

    private PdfPTable createTotalTable(Event event) {
        long reservationCount = reservationRepository.count();
        Iterable<Reservation> reservations = reservationRepository.findAll();
        List<Item> soldItems = ItemRepository.findBySoldNotNull(event);

        PdfPTable table = new PdfPTable(6);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        double sum = 0;
        for (Item item : soldItems) {
            sum += item.price.doubleValue();
        }

        double kitaSum = sum * CHILDCARE_SHARE;
        double totalEntryFees = ENTRY_FEE * reservationCount;
        double totalSum = kitaSum + totalEntryFees;

        addTotalLine(table, "Anzahl verkaufter Artikel", Integer.toString(soldItems.size()), true, 12);
        addTotalLine(table, "Summe verkaufter Artikel", currency.format(sum), true, 12);
        addTotalLine(table, "Kommissionsanteil (" + percent.format(CHILDCARE_SHARE) + ")", currency.format(kitaSum), false, 12);
        addTotalLine(table, "Teilnahmegebühren für " + reservationCount + " Teilnehmer", currency.format(totalEntryFees), false, 12);
        addTotalLine(table, "Gewinn insgesamt", currency.format(totalSum), true, 14);

        return table;
    }
}
