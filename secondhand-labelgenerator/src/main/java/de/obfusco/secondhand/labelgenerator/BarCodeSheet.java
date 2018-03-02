package de.obfusco.secondhand.labelgenerator;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;

@Component
class BarCodeSheet {

    private static final int NUMBER_OF_COLUMNS = 4;
    @Autowired
    ItemRepository itemRepository;
    private List<Item> items;

    private Path createPdf(Path targetPath) throws IOException,
            DocumentException {

        Files.createDirectories(targetPath);
        Document document = new Document(PageSize.A4,
                Utilities.millimetersToPoints(6), Utilities.millimetersToPoints(6),
                Utilities.millimetersToPoints(10), Utilities.millimetersToPoints(10));
        String filename = "barcodes.pdf";
        Path filePath = Paths.get(targetPath.toString(), filename);
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(filePath.toFile()));
        document.open();

        float cellHeight = 56;
        PdfPTable table = createTableLine();
        for (Item item : items) {
            table.addCell(createTableCell(writer, item, cellHeight));
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

    private PdfPTable createTableLine() {
        PdfPTable table = new PdfPTable(NUMBER_OF_COLUMNS);
        table.setSpacingBefore(0);
        table.setSpacingAfter(0);
        table.setWidthPercentage(100);
        return table;
    }

    private PdfPCell createTableCell(PdfWriter writer, Item item, float height) {
        PdfContentByte cb = writer.getDirectContent();

        Barcode128 barcode = new Barcode128();
        barcode.setCodeType(Barcode.CODE128);
        barcode.setCode(item.code);

        Image barcodeImage = barcode.createImageWithBarcode(cb, null, null);
        PdfPCell cell = new PdfPCell(barcodeImage);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(0);
        cell.setBorderWidth(0);
        cell.setFixedHeight(height);
        return cell;
    }

    public Path createPdfFile(Path basePath, Reservation reservation) throws IOException, DocumentException {
        String customer = new DecimalFormat("000").format(reservation.number);
        Path targetPath = Paths.get(basePath.toString(), customer);
        Files.createDirectories(targetPath);
        items = itemRepository.findByReservationOrderByNumberAsc(reservation);
        return createPdf(targetPath);
    }
}
