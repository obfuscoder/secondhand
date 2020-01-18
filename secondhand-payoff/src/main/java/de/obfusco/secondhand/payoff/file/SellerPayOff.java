package de.obfusco.secondhand.payoff.file;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import de.obfusco.secondhand.payoff.Rounder;
import de.obfusco.secondhand.storage.model.Event;
import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.model.Seller;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class SellerPayOff extends BasePayOff {

    private static final int NUMBER_OF_COLUMNS = 3;
    private static final int NUMBER_OF_ITEMS_PER_LINE = 2;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    EventRepository eventRepository;

    public File createFile(Path basePath, Reservation reservation) throws DocumentException, IOException {
        Path targetPath = Paths.get(basePath.toString(), Integer.toString(reservation.number));
        Files.createDirectories(targetPath);
        Path fullPath = Paths.get(targetPath.toString(), "payoff.pdf");
        Document document = createPdfDocument(fullPath);
        addContent(document, reservation);
        document.close();
        return fullPath.toFile();
    }

    private void addContent(Document document, Reservation reservation) throws DocumentException {
        Event event = eventRepository.find();
        addHeader(document, event);
        document.add(new Phrase("\n\n"));

        Seller seller = reservation.seller;
        document.add(new Phrase(seller.getName() + "\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));
        document.add(new Phrase(seller.street + "\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));
        document.add(new Phrase(seller.zipCode + " " + seller.city + "\n",
                FontFactory.getFont(FontFactory.HELVETICA, 12)));
        document.add(new Phrase("Tel.: " + seller.phone + "\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));

        document.add(new Phrase("Reservierungsnummer: " + reservation.number + "\n\n",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        List<Item> soldItems = itemRepository.findByReservationAndSoldNotNullOrderByNumberAsc(reservation);

        document.add(new Phrase(new Chunk(soldItems.size() + " Artikel verkauft",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));

        double totalPrice = 0;
        for (Item item : soldItems) {
            totalPrice += item.price.doubleValue();
        }
        double pricePrecision = event.pricePrecision.doubleValue();
        double commissionCut = Rounder.round(totalPrice * reservation.commissionRate.doubleValue(), event.preciseBillAmounts, pricePrecision);
        double totalSum = totalPrice - commissionCut;
        if (event.incorporateReservationFee()) {
            totalSum -= reservation.fee.doubleValue();
        }

        PdfPTable table = createItemTable(soldItems);
        addTotalLine(table, "Summe", true, 10, null, currency.format(totalPrice));
        String commissionText = generateCommissionText(event, reservation, pricePrecision);
        addTotalLine(table, commissionText, false, 10, null, currency.format(-commissionCut));
        if (event.incorporateReservationFee()) {
            addTotalLine(table, "Reservierungsgebühr", false, 10, null, currency.format(-reservation.fee.doubleValue()));
        }
        addTotalLine(table, "Auszuzahlender Betrag", true, 12, null, currency.format(totalSum));
        document.add(table);
        document.add(new Phrase("\n"));

        List<Item> returnedItems = event.gates ?
                itemRepository.findByReservationAndCheckedInNotNullAndSoldNullAndDonationFalseOrderByNumberAsc(reservation) :
                itemRepository.findByReservationAndSoldNullAndDonationFalseOrderByNumberAsc(reservation);
        document.add(new Phrase(new Chunk(returnedItems.size() + " Artikel zurück",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
        table = createItemTable(returnedItems);
        document.add(table);

        if (event.donationOfUnsoldItemsEnabled) {
            List<Item> donatedItems = event.gates ?
                    itemRepository.findByReservationAndCheckedInNotNullAndSoldNullAndDonationTrueOrderByNumberAsc(reservation) :
                    itemRepository.findByReservationAndSoldNullAndDonationTrueOrderByNumberAsc(reservation);
            if (donatedItems.size() > 0) {
                document.add(new Phrase("\n"));
                document.add(new Phrase(new Chunk(donatedItems.size() + " Artikel gespendet",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
                table = createItemTable(donatedItems);
                document.add(table);
            }
        }

        if (event.gates) {
            document.newPage();
            document.add(new Phrase(new Chunk("Artikelübersicht\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));
            document.add(new Phrase(new Chunk("grau = Artikel nicht abgegeben\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.GRAY))));
            document.add(new Phrase(new Chunk("grün = Artikel verkauft\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.GREEN))));
            document.add(new Phrase(new Chunk("blau = Artikel gespendet\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLUE))));
            document.add(new Phrase(new Chunk("schwarz = Artikel zurück\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK))));
            document.add(new Phrase(new Chunk("rot = Artikel fehlt\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.RED))));

            List<Item> items = itemRepository.findByReservationOrderByNumberAsc(reservation);
            table = createItemGateTable(items);
            document.add(table);
        }
    }

    private String generateCommissionText(Event event, Reservation reservation, double pricePrecision) {
        if (event.preciseBillAmounts) {
            return String.format("Kommissionsanteil (%s)",
                    percent.format(reservation.commissionRate.doubleValue()));
        } else {
            return String.format("Kommissionsanteil (%s - hinsichtlich Auszahlung auf %s bereinigt)",
                    percent.format(reservation.commissionRate.doubleValue()),
                    currency.format(pricePrecision));
        }
    }

    private PdfPTable createItemTable(List<Item> items) throws DocumentException {
        PdfPTable table = new PdfPTable(NUMBER_OF_COLUMNS * NUMBER_OF_ITEMS_PER_LINE);
        table.setWidthPercentage(100f);
        table.setWidths(new int[]{1, 7, 3, 1, 7, 3});
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        if (!items.isEmpty()) {
            addItemHeader(table);
        }

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get((i % 2 == 0) ? i / 2 : i / 2 + (items.size() + 1) / 2);
            for (int column = 0; column < NUMBER_OF_COLUMNS; column++) {
                PdfPCell cell = new PdfPCell(new Phrase(getColumnText(item, column),
                        FontFactory.getFont(FontFactory.HELVETICA, 10)));
                cell.setBorder(isFirstCellInLine(i, column) ? 0 : Rectangle.LEFT);
                cell.setBorderWidth(1);
                cell.setHorizontalAlignment(getColumnAlignment(column));
                table.addCell(cell);
            }
        }
        int linePosition = NUMBER_OF_ITEMS_PER_LINE - items.size() % NUMBER_OF_ITEMS_PER_LINE;
        for (int i = linePosition; i < NUMBER_OF_ITEMS_PER_LINE; i++) {
            for (int col = 0; col < NUMBER_OF_COLUMNS; col++) {
                PdfPCell cell = new PdfPCell(new Phrase("", FontFactory.getFont(FontFactory.HELVETICA, 10)));
                cell.setBorder(isFirstCellInLine(i, col) ? 0 : Rectangle.LEFT);
                cell.setBorderWidth(1);
                table.addCell(cell);
            }
        }
        return table;
    }

    private PdfPTable createItemGateTable(List<Item> items) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100f);
        table.setWidths(new int[] {1, 4, 8, 4, 2 });
        int[] alignments = { Element.ALIGN_RIGHT, Element.ALIGN_LEFT, Element.ALIGN_LEFT, Element.ALIGN_LEFT, Element.ALIGN_RIGHT };
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        addItemGateHeader(table);

        for (Item item : items) {
            for (int column = 0; column < table.getNumberOfColumns(); column++) {
                PdfPCell cell = new PdfPCell(new Phrase(getGateColumnText(item, column),
                        FontFactory.getFont(FontFactory.HELVETICA, 10, getItemColor(item))));
                cell.setBorder(column == 0 ? 0 : Rectangle.LEFT);
                cell.setBorderWidth(1);
                cell.setHorizontalAlignment(alignments[column]);
                table.addCell(cell);
            }
        }
        return table;
    }

    private BaseColor getItemColor(Item item) {
        if (!item.wasCheckedIn()) return BaseColor.GRAY;
        if (item.wasSold()) return BaseColor.GREEN;
        if (item.isDonated()) return BaseColor.BLUE;
        if (item.isMissing()) return BaseColor.RED;
        return BaseColor.BLACK;
    }

    private String getGateColumnText(Item item, int column) {
        switch (column) {
            case 0:
                return Integer.toString(item.number);
            case 1:
                return item.category.name;
            case 2:
                return item.description;
            case 3:
                return item.size;
            case 4:
                return currency.format(item.price);
            default:
                return StringUtils.EMPTY;
        }
    }

    private boolean isFirstCellInLine(int item, int column) {
        return column == 0 && item % NUMBER_OF_ITEMS_PER_LINE == 0;
    }

    private void addItemHeader(PdfPTable table) {
        String[] columnNames = {"Nr.", "Beschreibung", "Preis"};
        for (int c = 0; c < NUMBER_OF_ITEMS_PER_LINE; c++) {
            for (int i = 0; i < columnNames.length; i++) {
                String columnName = columnNames[i];
                PdfPCell cell = new PdfPCell(new Phrase(new Chunk(columnName,
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10))));
                cell.setBorder((isFirstCellInLine(c, i) ? 0 : Rectangle.LEFT) | Rectangle.BOTTOM);
                cell.setBorderWidth(1);
                table.addCell(cell);
            }
        }
        table.setHeaderRows(1);
    }

    private void addItemGateHeader(PdfPTable table) {
        String[] columnNames = { "Nr.", "Kategorie", "Beschreibung", "Größe", "Preis" };
        for (int c = 0; c < columnNames.length; c++) {
            String columnName = columnNames[c];
            PdfPCell cell = new PdfPCell(new Phrase(new Chunk(columnName,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10))));
            cell.setBorder((c == 0 ? 0 : Rectangle.LEFT) | Rectangle.BOTTOM);
            cell.setBorderWidth(1);
            table.addCell(cell);
        }
        table.setHeaderRows(1);
    }

    public File createFileForAll(Path basePath, Iterable<Reservation> reservations) throws IOException, DocumentException {
        Path targetPath = Paths.get(basePath.toString());
        Files.createDirectories(targetPath);
        Path fullPath = Paths.get(targetPath.toString(), "allpayoff.pdf");
        Document document = createPdfDocument(fullPath);
        for (Reservation reservation : reservations) {
            addContent(document, reservation);
            document.newPage();
        }
        document.close();
        return fullPath.toFile();
    }

    private Document createPdfDocument(Path fullPath) throws FileNotFoundException, DocumentException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 40);
        PdfWriter.getInstance(document,
                new FileOutputStream(fullPath.toFile()));
        document.open();
        return document;
    }

    private int getColumnAlignment(int column) {
        switch (column) {
            case 0:
            case 2:
                return Element.ALIGN_RIGHT;
            default:
                return Element.ALIGN_LEFT;
        }
    }

    private String getColumnText(Item item, int column) {
        switch (column) {
            case 0:
                return Integer.toString(item.number);
            case 1:
                return item.description;
            case 2:
                return currency.format(item.price);
            default:
                return StringUtils.EMPTY;
        }
    }
}
