package de.obfusco.secondhand.payoff.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.model.Seller;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SellerPayOff extends BasePayOff {

    @Autowired
    ReservedItemRepository reservedItemRepository;

    public static final int NUMBER_OF_COLUMNS = 3;
    public static final int NUMBER_OF_ITEMS_PER_LINE = 2;

    public File createFile(Path basePath, Reservation reservation) throws DocumentException, IOException {
        Path targetPath = Paths.get(basePath.toString(), reservation.getNumber().toString());
        Files.createDirectories(targetPath);
        Path fullPath = Paths.get(targetPath.toString(), "payoff.pdf");
        Document document = createPdfDocument(fullPath);
        addContentToPdf(document, reservation);
        document.close();
        return fullPath.toFile();
    }

    private void addContentToPdf(Document document, Reservation reservation) throws DocumentException {
        addHeader(document, reservation.getEvent());
        document.add(new Phrase("\n\n"));

        Seller seller = reservation.getSeller();
        document.add(new Phrase(seller.getName() + "\n", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        document.add(new Phrase(seller.getStreet() + "\n", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        document.add(new Phrase(seller.getZipCode() + " " + seller.getCity() + "\n\n", FontFactory.getFont(FontFactory.HELVETICA, 10)));

        document.add(new Phrase("Reservierungsnummer: " + reservation.getNumber() + "\n\n"));
        List<ReservedItem> soldItems = reservedItemRepository.findByReservationAndSoldNotNull(reservation);

        document.add(new Phrase(new Chunk(soldItems.size() + " Artikel wurde(n) verkauft",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));

        double totalPrice = 0;
        for (ReservedItem item : soldItems) {
            totalPrice += item.getItem().getPrice().doubleValue();
        }
        double kitaSum = totalPrice * CHILDCARE_SHARE;
        kitaSum = Math.ceil(kitaSum * 10) / 10;
        double totalSum = totalPrice - (kitaSum + ENTRY_FEE);

        PdfPTable table = createItemTable(soldItems);
        addTotalLine(table, "Summe", currency.format(totalPrice), true);
        addTotalLine(table, "Erlös Kita (" + percent.format(CHILDCARE_SHARE) + " auf 10 Cent aufgerundet)", currency.format(-kitaSum), false);
        addTotalLine(table, "Reservierungsgebühr", currency.format(-ENTRY_FEE), false);
        addTotalLine(table, "Gewinn", currency.format(totalSum), true);
        document.add(table);
        document.add(new Phrase("\n"));

        List<ReservedItem> unsoldItems = reservedItemRepository.findByReservationAndSoldNull(reservation);
        document.add(new Phrase(new Chunk(unsoldItems.size() + " Artikel wurde(n) nicht verkauft",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))));

        totalPrice = 0;
        for (ReservedItem item : unsoldItems) {
            totalPrice += item.getItem().getPrice().doubleValue();
        }
        table = createItemTable(unsoldItems);
        addTotalLine(table, "Summe", currency.format(totalPrice), true);
        document.add(table);
    }

    private PdfPTable createItemTable(List<ReservedItem> items) throws DocumentException {
        PdfPTable table = new PdfPTable(NUMBER_OF_COLUMNS*NUMBER_OF_ITEMS_PER_LINE);
        table.setWidthPercentage(100f);
        table.setWidths(new int[]{1, 2, 1, 1, 2, 1});
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        if (!items.isEmpty()) {
            addItemHeader(table);
        }

        for (int i = 0; i < items.size(); i++) {
            ReservedItem item = items.get(i);
            for (int col = 0; col < NUMBER_OF_COLUMNS; col++) {
                PdfPCell cell = new PdfPCell(new Phrase(getColText(item, i, col),
                        FontFactory.getFont(FontFactory.HELVETICA, 10)));
                cell.setBorder(isFirstCellInLine(i, col) ? 0 : Rectangle.LEFT);
                cell.setBorderWidth(1);
                table.addCell(cell);
            }
        }
        int linePosition = NUMBER_OF_ITEMS_PER_LINE - items.size() % NUMBER_OF_ITEMS_PER_LINE;
        for (int i=linePosition; i<NUMBER_OF_ITEMS_PER_LINE; i++) {
            for (int col = 0; col < NUMBER_OF_COLUMNS; col++) {
                PdfPCell cell = new PdfPCell(new Phrase("", FontFactory.getFont(FontFactory.HELVETICA, 10)));
                cell.setBorder(isFirstCellInLine(i, col) ? 0 : Rectangle.LEFT);
                cell.setBorderWidth(1);
                table.addCell(cell);
            }
        }
        return table;
    }

    private boolean isFirstCellInLine(int item, int col) {
        return col == 0 && item %NUMBER_OF_ITEMS_PER_LINE == 0;
    }

    private void addItemHeader(PdfPTable table) {
        String[] columnNames = {"Pos", "ArtNr", "Preis"};
        for (int c=0; c<NUMBER_OF_ITEMS_PER_LINE; c++) {
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

    public File createFileForAll(Path basePath, List<Reservation> reservations) throws IOException, DocumentException {
        Path targetPath = Paths.get(basePath.toString());
        Files.createDirectories(targetPath);
        Path fullPath = Paths.get(targetPath.toString(), "allpayoff.pdf");
        Document document = createPdfDocument(fullPath);
        for (Reservation reservation : reservations) {
            addContentToPdf(document, reservation);
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

    private String getColText(ReservedItem item, int i, int col) {
        switch (col) {
            case 0:
                return Integer.toString(i + 1);
            case 1:
                return item.getCode();
            case 2:
                return currency.format(item.getItem().getPrice());
            default:
                return StringUtils.EMPTY;
        }
    }
}
