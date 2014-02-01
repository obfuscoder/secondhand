package de.obfusco.secondhand.barcodefilegenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class BarCodeSheet {

    @Autowired
    ReservedItemRepository reservedItemRepository;

    public static final int NUMBER_OF_COLUMNS = 4;

    /**
     * The resulting PDF.
     */
    private static final String RESULT = "C:\\flohmarkt\\result_o_b.pdf";
    private static final String resultpath = "C:\\flohmarkt\\completion\\";

    @SuppressWarnings("unchecked")
    public BarCodeSheet() {

    }

    public String createPdf(String path) throws IOException,
            DocumentException {

        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        String filename = "result_o_b.pdf";
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(filename));
        document.open();

        PdfPTable table = null;
        for (int i = 0; i < items.size(); i++) {
            if (i % NUMBER_OF_COLUMNS == 0) {
                if (table != null) {
                    document.add(table);
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
        table.setSpacingBefore(0);
        table.setSpacingAfter(0);
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

        PdfPTable table = new PdfPTable(6);
        table.setTotalWidth(100);
        table.setLockedWidth(true);

        PdfPCell cell = new PdfPCell(new Phrase((new Chunk(item.getCode().substring(5, 7),
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

    List<ReservedItem> items;

    public String createPDFFile(Reservation reservation) throws IOException, DocumentException {
        String customer = new DecimalFormat("000").format(reservation.getNumber());
        String path = resultpath + customer + "\\";
        (new File(path)).mkdirs();
        items = reservedItemRepository.findByReservation(reservation);

        String filename = createPdf(path);

        return filename;
    }
}
