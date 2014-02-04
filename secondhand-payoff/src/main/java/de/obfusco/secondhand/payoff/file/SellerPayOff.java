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
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.model.Seller;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SellerPayOff extends BasePayOff {

    @Autowired
    ReservedItemRepository reservedItemRepository;

    public File createFile(String path, Reservation reservation) throws DocumentException, FileNotFoundException {

        String fullPath = path + reservation.getNumber() + "_payoff.pdf";
        Document document = new Document(PageSize.A4,80, 50, 50, 30);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fullPath));
        document.open();
        addHeader(document);
        document.add(new Phrase("\n\n"));

        Seller seller = reservation.getSeller();
        document.add(new Phrase(seller.getName() + "\n"));
        document.add(new Phrase(seller.getStreet() + "\n"));
        document.add(new Phrase(seller.getZipCode() + " " + seller.getCity() + "\n\n"));
        document.add(new Phrase("Tel: " + seller.getPhone() + "\n"));
        document.add(new Phrase("Email: " + seller.getEmail()));
        document.add(new Phrase("\n\n"));

        document.add(new Phrase("Reservierungsnummer: " + reservation.getNumber() + "\n\n"));
        List<ReservedItem> soldItems = reservedItemRepository.findByReservationAndSoldNotNull(reservation);

        document.add(new Phrase(new Chunk(soldItems.size() + " Artikel wurde(n) verkauft",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18))));

        double totalPrice = 0;
        for (ReservedItem item : soldItems) {
            totalPrice += item.getItem().getPrice().doubleValue();
        }
        double kitaSum = totalPrice * CHILDCARE_SHARE;
        double totalSum = totalPrice - (kitaSum + ENTRY_FEE);

        PdfPTable table = createItemTable(soldItems);
        addSeparatorLine(table);
        addTotalLine(table, "Summe", currency.format(totalPrice), true);
        addTotalLine(table, "Erlös Kita (" + percent.format(CHILDCARE_SHARE) + ")", currency.format(kitaSum), false);
        addTotalLine(table, "Teilnahmegebühr", currency.format(ENTRY_FEE), false);
        addTotalLine(table, "Gewinn", currency.format(totalSum), true);
        document.add(table);
        document.add(new Phrase("\n"));

        List<ReservedItem> unsoldItems = reservedItemRepository.findByReservationAndSoldNull(reservation);
        document.add(new Phrase(new Chunk(unsoldItems.size() + " Artikel wurde(n) nicht verkauft",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18))));

        totalPrice = 0;
        for (ReservedItem item : unsoldItems) {
            totalPrice += item.getItem().getPrice().doubleValue();
        }
        table = createItemTable(unsoldItems);
        addSeparatorLine(table);
        addTotalLine(table, "Summe", currency.format(totalPrice), true);
        document.add(table);
        document.close();
        return new File(fullPath);
    }

    private void addSeparatorLine(PdfPTable table) {
        PdfPCell cell = new PdfPCell(new Phrase("------------------------------------------------------------"));
        cell.setColspan(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private PdfPTable createItemTable(List<ReservedItem> items) {
        PdfPTable table = new PdfPTable(5);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        String[] columnNames = {"Pos", "ArtNr", "Kategorie", "Größe", "Preis"};
        for (String columnName : columnNames) {
            table.addCell(new PdfPCell(new Phrase(new Chunk(columnName,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)))));
        }

        for (int i = 0; i < items.size(); i++) {
            ReservedItem item = items.get(i);
            PdfPCell cell = new PdfPCell(new Phrase("" + (i + 1)));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(item.getCode()));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(item.getItem().getCategory().getName()));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(item.getItem().getSize()));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(currency.format(item.getItem().getPrice())));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            //new Row
            cell = new PdfPCell(new Phrase(item.getItem().getDescription()));
            cell.setColspan(5);
            table.addCell(cell);
        }
        return table;
    }
}
