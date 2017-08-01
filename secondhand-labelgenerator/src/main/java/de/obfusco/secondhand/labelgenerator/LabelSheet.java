package de.obfusco.secondhand.labelgenerator;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import de.obfusco.secondhand.storage.model.Item;
import de.obfusco.secondhand.storage.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
public class LabelSheet {

    public static final int NUMBER_OF_COLUMNS = 3;
    public static final int NUMBER_OF_ROWS = 5;

    List<Item> items;

    @Autowired
    de.obfusco.secondhand.storage.repository.ItemRepository ItemRepository;

    public Path createPdf(Path targetPath) throws DocumentException, IOException {

        Document document = new Document(PageSize.A4,
                Utilities.millimetersToPoints(3), Utilities.millimetersToPoints(5),
                Utilities.millimetersToPoints(22), Utilities.millimetersToPoints(22));
        Files.createDirectories(targetPath);
        Path filePath = Paths.get(targetPath.toString(), "labels.pdf");
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
        document.open();

        PdfPTable table = createTable();
        for (int i = 0; i < items.size(); i++) {
            PdfPCell cell = createTableCell(writer, items.get(i));
            cell.setPadding(6);
            table.addCell(cell);
        }
        table.completeRow();
        document.add(table);
        document.close();
        return filePath;
    }

    public Path createPdfFile(Path basePath, Reservation reservation) throws DocumentException, IOException {
        String customer = new DecimalFormat("000").format(reservation.number);
        Path targetPath = Paths.get(basePath.toString(), customer);
        Files.createDirectories(targetPath);
        items = ItemRepository.findByReservationOrderByNumberAsc(reservation);
        return createPdf(targetPath);
    }

    private Element createCellContent(PdfWriter writer, Item item) throws IOException, DocumentException {
        PdfContentByte cb = writer.getDirectContent();
        PdfPTable table = new PdfPTable(12);
        table.setWidthPercentage(100);

        PdfPCell cell = createNumberCell(item);
        cell.setColspan(6);
        cell.setFixedHeight(getLabelCellHeight(writer) / 11 * 2);
        table.addCell(cell);

        cell = createPriceCell(item);
        cell.setColspan(6);
        cell.setFixedHeight(getLabelCellHeight(writer) / 11 * 2);
        table.addCell(cell);

        int colSpanForDetails = table.getNumberOfColumns();
        int colSpanForDonation = 2;
        if (item.isDonation()) {
            PdfPCell donationCell = createDonationCell();
            donationCell.setColspan(colSpanForDonation);
            table.addCell(donationCell);
            colSpanForDetails = table.getNumberOfColumns() - colSpanForDonation;
        }

        cell = createDetailsCell(item);
        cell.setColspan(colSpanForDetails);
        cell.setFixedHeight(getLabelCellHeight(writer) / 11 * 5);
        table.addCell(cell);

        Barcode128 barcode = new Barcode128();
        barcode.setCodeType(Barcode.CODE128);
        barcode.setCode(item.code);

        cell = createBarcodeCell(cb, barcode);
        cell.setColspan(table.getNumberOfColumns());
        cell.setFixedHeight(getLabelCellHeight(writer) / 11 * 4);
        table.addCell(cell);
        return table;
    }

    private PdfPCell createDonationCell() {
        PdfPCell cell = new PdfPCell(new Phrase(new Chunk("S", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30))));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BaseColor.BLACK);
        cell.setBorderWidth(1);
        return cell;
    }

    private PdfPCell createBarcodeCell(PdfContentByte cb, Barcode128 codeEAN) {
        Image barcode = codeEAN.createImageWithBarcode(cb, null, null);
        PdfPCell cell = new PdfPCell(barcode);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(BaseColor.BLACK);
        cell.setBorderWidth(1);
        return cell;
    }

    private PdfPCell createPriceCell(Item item) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        String price = currencyFormat.format(item.price);
        PdfPCell cell = new PdfPCell(new Phrase(price, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20)));
        cell.setBorderColor(BaseColor.BLACK);
        cell.setBorderWidth(1);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell createDetailsCell(Item item) {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getCategoryName());
        sb.append("\n");
        sb.append(item.description);
        if (item.getSize() != null && item.getSize().length() > 0) {
            sb.append("\nGröße: ");
            sb.append(item.getSize());
        }
        PdfPCell cell = new PdfPCell(new Phrase(new Chunk(sb.toString(),
                FontFactory.getFont(FontFactory.HELVETICA, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BaseColor.BLACK);
        cell.setBorderWidth(1);
        return cell;
    }

    private PdfPCell createNumberCell(Item item) {
        PdfPCell cell = new PdfPCell(new Phrase(new Chunk(
                String.format("%d - %d", item.getReservation().number, item.number),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20))));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BaseColor.BLACK);
        cell.setBorderWidth(1);
        return cell;
    }

    private PdfPCell createTableCell(PdfWriter writer, Item item) throws IOException, DocumentException {
        PdfPCell cell = new PdfPCell(new Paragraph(""));
        cell.setBorder(0);
        cell.addElement(createCellContent(writer, item));
        cell.setFixedHeight(getLabelCellHeight(writer));
        return cell;
    }

    private float getLabelCellHeight(PdfWriter writer) {
        return (writer.getPageSize().getHeight() - Utilities.millimetersToPoints(45)) / NUMBER_OF_ROWS;
    }

    private PdfPTable createTable() {
        PdfPTable table = new PdfPTable(NUMBER_OF_COLUMNS);
        table.setWidthPercentage(100);
        return table;
    }

}
