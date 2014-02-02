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

import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.model.Seller;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SellerPayOff {

    String path = "C:\\flohmarkt\\Abrechnung\\";
    String completeBillpath = "C:\\flohmarkt\\KomplettAbrechnung\\";
    String filename;
    private static final double ENTRY_FEE = 2.5;
    String customer;

    @Autowired
    ReservedItemRepository reservedItemRepository;

    public SellerPayOff() {
        path = path + customer + "\\";
        filename = "total_payoff.pdf";
    }

    public File createFile(Reservation reservation) throws DocumentException, FileNotFoundException {
        boolean success = (new File(path)).mkdirs();
        if (!success) {
            System.out.println("create Dir failed");
        }

        String fullPath = path + reservation.getNumber() + "_payoff.pdf";
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(fullPath));
        document.open();

        document.add(new Phrase(new Chunk("Abrechnung Flohmarkt", FontFactory
                .getFont(FontFactory.HELVETICA_BOLD, 24))));

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

        double kitaSum = 0;
        double totalPrice = 0.0;
        for (ReservedItem item : soldItems) {
            totalPrice = totalPrice * 100 + item.getItem().getPrice().doubleValue() * 100;
            totalPrice /= 100;
        }

        kitaSum = totalPrice * 0.2;

        double totalSum = totalPrice * 100 - (kitaSum * 100 + ENTRY_FEE * 100);
        totalSum /= 100;

        document.add(createItemTable(soldItems, totalPrice, true, kitaSum, ENTRY_FEE, totalSum));
        document.add(new Phrase("\n"));
        List<ReservedItem> unsoldItems = reservedItemRepository.findByReservationAndSoldNull(reservation);
        document.add(new Phrase(new Chunk(unsoldItems.size() + " Artikel wurde(n) nicht verkauft",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18))));

        totalPrice = 0;
        for (ReservedItem item : unsoldItems) {
            totalPrice = totalPrice * 100 + item.getItem().getPrice().doubleValue() * 100;
            totalPrice /= 100;
        }
        document.add(createItemTable(unsoldItems, totalPrice));

        document.close();
        return new File(fullPath);
    }

    public static PdfPTable createItemTable(List<ReservedItem> items, double sum) {
        return createItemTable(items, sum, false, 0, 0, 0);
    }

    public static PdfPTable createItemTable(List<ReservedItem> items, double sum, boolean addFinalSums, double kitaSum, double entryFee, double totalSum) {

        PdfPTable table = new PdfPTable(5);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        PdfPCell cell;

        cell = new PdfPCell(new Phrase(new Chunk("Pos", FontFactory.getFont(
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

        for (int i = 0; i < items.size(); i++) {
            ReservedItem item = items.get(i);
            cell = new PdfPCell(new Phrase("" + (i + 1)));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(item.getCode()));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(item.getItem().getCategory().getName()));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(item.getItem().getSize()));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase("" + item.getItem().getPrice().floatValue()));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            //new Row
            cell = new PdfPCell(new Phrase(item.getItem().getDescription()));
            cell.setColspan(5);
            table.addCell(cell);
        }

        cell = new PdfPCell(new Phrase("------------------------------------------------------------"));
        cell.setColspan(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk("Summe", FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        cell.setColspan(4);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(sum + " EURO", FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        if (addFinalSums) {
            cell = new PdfPCell(new Phrase(new Chunk("Erlös Kita (20%)", FontFactory.getFont(
                    FontFactory.HELVETICA, 12))));
            cell.setColspan(4);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(new Chunk("- " + kitaSum + " EURO", FontFactory.getFont(
                    FontFactory.HELVETICA, 12))));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(new Chunk("Teilnahmegebühr", FontFactory.getFont(
                    FontFactory.HELVETICA, 12))));
            cell.setColspan(4);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(new Chunk("- " + entryFee + " EURO", FontFactory.getFont(
                    FontFactory.HELVETICA, 12))));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(new Chunk("Gewinn", FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 12))));
            cell.setColspan(4);
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(new Chunk(totalSum + " EURO", FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 12))));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
        }

        return table;
    }
}
