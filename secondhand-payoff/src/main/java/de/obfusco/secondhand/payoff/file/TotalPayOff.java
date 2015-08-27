package de.obfusco.secondhand.payoff.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TotalPayOff extends BasePayOff {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EventRepository eventRepository;

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

        PdfPTable table = new PdfPTable(6);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        double pricePrecision = event.pricePrecision.doubleValue();

        double commissionSum = 0.0;
        double feeSum = 0.0;
        double soldSum = 0.0;
        for (Reservation reservation : reservations) {
            double sum = 0;
            for (Item item : itemRepository.findByReservationAndSoldNotNullOrderByNumberAsc(reservation)) {
                sum += item.price.doubleValue();
            }
            soldSum += sum;
            sum *= event.commissionRate.doubleValue();
            commissionSum += Math.ceil(sum/pricePrecision) * pricePrecision;
            feeSum += event.sellerFee.doubleValue();
        }

        int soldItemCount = itemRepository.findBySoldNotNull().size();
        addTotalLine(table, "Anzahl verkaufter Artikel", Integer.toString(soldItemCount), true, 12);
        addTotalLine(table, "Summe verkaufter Artikel", currency.format(soldSum), true, 12);
        addTotalLine(table, "Kommissionsanteil (" + percent.format(event.commissionRate) + ")", currency.format(commissionSum), false, 12);
        addTotalLine(table, "Teilnahmegebühren für " + reservationCount + " Teilnehmer", currency.format(feeSum), false, 12);
        addTotalLine(table, "Gewinn insgesamt", currency.format(commissionSum + feeSum), true, 14);

        return table;
    }
}
