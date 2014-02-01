package de.obfusco.secondhand.barcodefilegenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.List;

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

@Component
public class BarCodeLabelSheet {

    public static final int NUMBER_OF_ROWS = 5;
    public static final int NUMBER_OF_COLUMNS = 3;

    private static final String RESULT = "C:\\flohmarkt\\resultLabel.pdf";
    private static final String resultpath = "C:\\flohmarkt\\completion\\";

    @Autowired
    ReservedItemRepository reservedItemRepository;

    @SuppressWarnings("unchecked")
    public BarCodeLabelSheet() {

    }

    public String createPdf(String path) throws DocumentException, FileNotFoundException {

        Document document = new Document(PageSize.A4, 0, 0, 40, 40);
        String filename = path + "resultLabel.pdf";
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(filename));
        document.open();

        PdfPTable table = null;
        for (int i = 0; i < items.size(); i++) {
            if (i % NUMBER_OF_COLUMNS == 0) {
                if (table != null) {
                    document.add(table);
                    if (i / NUMBER_OF_COLUMNS % NUMBER_OF_ROWS == 0) {
                        document.newPage();
                    }
                }
                table = createTableLine();
            }
            table.addCell(createTableCell(writer, items.get(i)));
        }
        if (table != null) {
            document.add(table);
        }
        document.close();
        return filename;
    }

    private PdfPTable createTableLine() {
        PdfPTable table = new PdfPTable(NUMBER_OF_COLUMNS);
        table.setWidthPercentage(100);
        return table;
    }

    private PdfPCell createTableCell(PdfWriter writer, ReservedItem item) {
        PdfPCell cell = new PdfPCell(new Paragraph(""));
        cell.setBorderColor(BaseColor.WHITE);
        cell.addElement(createCellContent(writer, item));
        return cell;
    }

    private Element createCellContent(PdfWriter writer, ReservedItem item) {
        PdfContentByte cb = writer.getDirectContent();

        Barcode128 codeEAN = new Barcode128();
        codeEAN.setCodeType(Barcode.CODE128);
        codeEAN.setCode(item.getCode());

        PdfPTable table = new PdfPTable(NUMBER_OF_COLUMNS);
        table.setTotalWidth(180);
        table.setLockedWidth(true);

        PdfPCell cell = new PdfPCell(new Phrase(new Chunk(
                item.getReservation().getNumber().toString(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18))));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk(item.getCode().substring(5, 7),
                FontFactory.getFont(FontFactory.HELVETICA, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setColspan(2);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Bez.:"));
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk(item.getItem().getDescription(),
                FontFactory.getFont(FontFactory.HELVETICA, 10))));
        cell.setColspan(2);
        cell.setRowspan(NUMBER_OF_COLUMNS);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorder(NUMBER_OF_COLUMNS);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(" "));
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(" "));
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Größe:"));
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(item.getItem().getSize()));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(""));
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Preis:"));
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        String price = String.format("%.2f", item.getItem().getPrice());
        cell = new PdfPCell(new Phrase(price));
        cell.setBorderColor(BaseColor.WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Euro"));
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        Image barcode = codeEAN.createImageWithBarcode(cb, null, null);
        barcode.scalePercent(120f);
        cell = new PdfPCell(barcode);
        cell.setColspan(NUMBER_OF_COLUMNS);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(2);
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        table.setSpacingAfter(2f);

        return table;
    }

    List<ReservedItem> items;

    public String createPDFFile(Reservation reservation) throws DocumentException, FileNotFoundException {
        String customer = new DecimalFormat("000").format(reservation.getNumber());
        String path = resultpath + customer + "\\";
        (new File(path)).mkdirs();
        items = reservedItemRepository.findByReservation(reservation);

        String filename = createPdf(path);

        return filename;
    }

}
