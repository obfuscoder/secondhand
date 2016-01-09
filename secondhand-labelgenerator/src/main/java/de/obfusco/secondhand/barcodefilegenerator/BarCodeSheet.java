package de.obfusco.secondhand.barcodefilegenerator;

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
public class BarCodeSheet {

    public static final int NUMBER_OF_COLUMNS = 4;
    @Autowired
    ItemRepository ItemRepository;
    List<Item> items;

    public Path createPdf(Path targetPath) throws IOException,
            DocumentException {

        Files.createDirectories(targetPath);
        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        String filename = "barcodes.pdf";
        Path filePath = Paths.get(targetPath.toString(), filename);
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

    private PdfPTable createTableLine() {
        PdfPTable table = new PdfPTable(NUMBER_OF_COLUMNS);
        table.setSpacingBefore(0);
        table.setSpacingAfter(0);
        table.setWidthPercentage(100);
        return table;
    }

    private PdfPCell createTableCell(PdfWriter writer, Item item) {
        PdfPCell cell = new PdfPCell(new Paragraph(""));
        cell.setBorderColor(BaseColor.WHITE);
        cell.addElement(createCellContent(writer, item));
        return cell;
    }

    private Element createCellContent(PdfWriter writer, Item item) {
        PdfContentByte cb = writer.getDirectContent();

        Barcode128 codeEAN = new Barcode128();
        codeEAN.setCodeType(Barcode.CODE128);
        codeEAN.setCode(item.code);

        PdfPTable table = new PdfPTable(6);
        table.setTotalWidth(100);
        table.setLockedWidth(true);

        PdfPCell cell = new PdfPCell(new Phrase((new Chunk(Integer.toString(item.number),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)))));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(BaseColor.WHITE);

        table.addCell(cell);

        Image barcode = codeEAN.createImageWithBarcode(cb, null, null);
        barcode.scalePercent(120f);
        cell = new PdfPCell(barcode);
        cell.setColspan(5);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6f);
        table.addCell(cell);
        table.setSpacingAfter(4f);
        return table;
    }

    public Path createPDFFile(Path basePath, Reservation reservation) throws IOException, DocumentException {
        String customer = new DecimalFormat("000").format(reservation.number);
        Path targetPath = Paths.get(basePath.toString(), customer);
        Files.createDirectories(targetPath);
        items = ItemRepository.findByReservationOrderByNumberAsc(reservation);
        return createPdf(targetPath);
    }
}