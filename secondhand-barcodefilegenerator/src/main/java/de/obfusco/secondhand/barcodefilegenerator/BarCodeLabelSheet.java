package de.obfusco.secondhand.barcodefilegenerator;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.model.ReservedItem;
import de.obfusco.secondhand.storage.repository.ReservedItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Component
public class BarCodeLabelSheet {

    public static final int NUMBER_OF_COLUMNS = 3;
    public static final int NUMBER_OF_ROWS = 5;

    List<ReservedItem> items;

    @Autowired
    ReservedItemRepository reservedItemRepository;

    public Path createPdf(Path targetPath) throws DocumentException, IOException {

        Document document = new Document(PageSize.A4, 0, 0,
                Utilities.millimetersToPoints(20), Utilities.millimetersToPoints(20));
        Files.createDirectories(targetPath);
        Path filePath = Paths.get(targetPath.toString(), "labels.pdf");
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(filePath.toFile()));
        document.open();

        PdfPTable table = createTableLine();
        for (int i = 0; i < items.size(); i++) {
            table.addCell(createTableCell(writer, items.get(i)));
        }
        for (int i=0; i<(NUMBER_OF_COLUMNS-(items.size() % NUMBER_OF_COLUMNS)) % NUMBER_OF_COLUMNS; i++) {
            PdfPCell cell = new PdfPCell(new Paragraph(""));
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
        }
        document.add(table);
        document.close();
        return filePath;
    }

    private void drawGrid(PdfWriter writer) {
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        cb.setColorStroke(BaseColor.BLACK);
        for(int c=0; c<3; c++) {
            for (int r=0; r<5; r++) {
                cb.rectangle(
                        Utilities.millimetersToPoints(c * 70),
                        Utilities.millimetersToPoints(r*51+20),
                        Utilities.millimetersToPoints(70),
                        Utilities.millimetersToPoints(51));
            }
        }
        cb.stroke();
        cb.restoreState();
    }

    public Path createPDFFile(Path basePath, Reservation reservation) throws DocumentException, IOException {
        String customer = new DecimalFormat("000").format(reservation.getNumber());
        Path targetPath = Paths.get(basePath.toString(), customer);
        Files.createDirectories(targetPath);
        items = reservedItemRepository.findByReservationOrderByNumberAsc(reservation);
        return createPdf(targetPath);
    }

    private Element createCellContent(PdfWriter writer, ReservedItem item) {
        PdfContentByte cb = writer.getDirectContent();

        Barcode128 barcode = new Barcode128();
        barcode.setCodeType(Barcode.CODE128);
        barcode.setCode(item.getCode());

        PdfPTable table = new PdfPTable(5);
        table.setTotalWidth(180);
        table.setLockedWidth(true);

        PdfPCell cell = createSellerNumberCell(item);
        table.addCell(cell);

        cell = createItemNumberCell(item);
        table.addCell(cell);

        cell = createCategoryCell(item);
        table.addCell(cell);

        cell = createDescriptionCell(item, table);
        table.addCell(cell);

        cell = createSizeCell(item);
        table.addCell(cell);

        cell = createPriceCell(item);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(" "));
        cell.setColspan(table.getNumberOfColumns());
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        cell = createBarcodeCell(cb, barcode);
        table.addCell(cell);

        table.setSpacingAfter(2f);

        return table;
    }

    private PdfPCell createBarcodeCell(PdfContentByte cb, Barcode128 codeEAN) {
        Image barcode = codeEAN.createImageWithBarcode(cb, null, null);
        barcode.scalePercent(120f);
        PdfPCell cell = new PdfPCell(barcode);
        cell.setColspan(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(2);
        cell.setBorderColor(BaseColor.WHITE);
        return cell;
    }

    private PdfPCell createPriceCell(ReservedItem item) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        String price = currencyFormat.format(item.getItem().getPrice());
        PdfPCell cell = new PdfPCell(new Phrase(price, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22)));
        cell.setBorderColor(BaseColor.WHITE);
        cell.setColspan(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell createSizeCell(ReservedItem item) {
        PdfPCell cell = new PdfPCell(new Phrase("Größe: " + item.getItem().getSize()));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setColspan(5);
        cell.setBorderColor(BaseColor.WHITE);
        return cell;
    }

    private PdfPCell createDescriptionCell(ReservedItem item, PdfPTable table) {

        PdfPCell cell = new PdfPCell(new Phrase(new Chunk(item.getItem().getDescription(),
                FontFactory.getFont(FontFactory.HELVETICA, 10))));
        cell.setColspan(table.getNumberOfColumns());
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorder(NUMBER_OF_COLUMNS);
        return cell;
    }

    private PdfPCell createCategoryCell(ReservedItem item) {
        PdfPCell cell = new PdfPCell(new Phrase(new Chunk(item.getItem().getCategory().getName(),
                FontFactory.getFont(FontFactory.HELVETICA, 10))));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setColspan(3);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createItemNumberCell(ReservedItem item) {
        PdfPCell cell = new PdfPCell(new Phrase(new Chunk(Integer.toString(item.getNumber()),
                FontFactory.getFont(FontFactory.HELVETICA, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_BASELINE);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createSellerNumberCell(ReservedItem item) {
        PdfPCell cell = new PdfPCell(new Phrase(new Chunk(
                item.getReservation().getNumber().toString(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18))));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        return cell;
    }

    private PdfPCell createTableCell(PdfWriter writer, ReservedItem item) {
        PdfPCell cell = new PdfPCell(new Paragraph(""));
        cell.setBorderColor(BaseColor.WHITE);
        cell.addElement(createCellContent(writer, item));
        return cell;
    }

    private PdfPTable createTableLine() {
        PdfPTable table = new PdfPTable(NUMBER_OF_COLUMNS);
        table.setWidthPercentage(100);
        return table;
    }

}
