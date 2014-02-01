package de.obfusco.secondhand.barcodefilegenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class BarcodePDFCreator {

    /**
     * The resulting PDF.
     */
    private static final String RESULT = "C:\\Users\\Anne\\flohmarkt\\result.pdf";
    private static final String resultpath = "C:\\Users\\Anne\\flohmarkt\\Customer\\";

    public BarcodePDFCreator() {

    }

    public void createPdf(String path, int customer) throws IOException,
            DocumentException {

        Document document = new Document();// (new Rectangle(340, 842));
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(path + "result.pdf"));
        document.open();

        int size = items.size();
        int modItems = size % 2;
        int tablelinecounter;
        tablelinecounter = size / 2;
        if (modItems > 0) {
            tablelinecounter++;
        }
        for (int j = 1; j <= tablelinecounter; j++) {
            int tablecounter = 2;
            if (j == tablelinecounter && modItems > 0) {
                tablecounter = modItems;
            }

            createTableLine(document, writer, tablecounter, customer);

            if (j % 3 == 0) {
                document.newPage();
            }
        }

        document.close();

    }

    private void createTableLine(Document document, PdfWriter writer, int count, int customer)
            throws DocumentException {

        PdfPTable table = new PdfPTable(3); // 3 columns.

        PdfPCell cell1 = new PdfPCell(new Paragraph(""));
        PdfPCell cell2 = new PdfPCell(new Paragraph(""));
        PdfPCell cell3 = new PdfPCell(new Paragraph(""));

        cell1.setBorderColor(BaseColor.WHITE);
        cell2.setBorderColor(BaseColor.WHITE);
        cell3.setBorderColor(BaseColor.WHITE);
        cell1.addElement(createTable(writer, document, customer));
        if (count > 1) {
            cell3.addElement(createTable(writer, document, customer));
        }

        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell3);

        document.add(table);

    }

    private Element createTable(PdfWriter writer, Document document, int customer)
            throws DocumentException {

        PdfContentByte cb = writer.getDirectContent();

        Object[] itemdata = (Object[]) items.get(itemkeys.get(itemCounter));

        // EAN 8
        Barcode128 codeEAN = new Barcode128();
        // document.add(new Paragraph("Barcode EAN.UCC-8"));
        codeEAN.setCodeType(Barcode.CODE128);
//		codeEAN.setBarHeight(codeEAN.getSize() * 3f);
        codeEAN.setCode((String) itemdata[0]);

        PdfPTable table = new PdfPTable(3);

        table.setSpacingBefore(10f);

        table.setTotalWidth(200);
        table.setLockedWidth(true);

        PdfPCell cell;
        // we add a cell with colspan 3
        cell = new PdfPCell(new Phrase("Zum Anbringen an der Ware"));
        cell.setColspan(3);
        table.addCell(cell);

        table.addCell("Art-Nr.");
        cell = new PdfPCell(new Phrase((String) itemdata[0]));
        cell.setColspan(2);
        cell.setSpaceCharRatio(60);
        table.addCell(cell);

        table.addCell("Preis");
        table.addCell((String) itemdata[4]);
        table.addCell("Euro");

        cell = new PdfPCell(new Phrase(
                "-------------------------------------------------"));
        cell.setColspan(3);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(new Chunk(Integer.toString(customer), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18))));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setColspan(3);
        table.addCell(cell);

        table.addCell("Art-Nr.");
        cell = new PdfPCell(new Phrase((String) itemdata[0]));
        cell.setColspan(2);
        table.addCell(cell);

        table.addCell("Bez.");
        cell = new PdfPCell(new Phrase((String) itemdata[2]));
        cell.setColspan(2);
        table.addCell(cell);

        table.addCell("Größe");
        cell = new PdfPCell(new Phrase((String) itemdata[3]));
        cell.setColspan(2);
        table.addCell(cell);

        table.addCell("Preis");
        table.addCell((String) itemdata[4]);
        table.addCell("Euro");

        Image barcode = codeEAN.createImageWithBarcode(cb, null, null);
        barcode.scalePercent(120f);
        cell = new PdfPCell(barcode);
        cell.setColspan(3);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10);
//		cell.setPaddingLeft(50);
        table.addCell(cell);

        table.setSpacingAfter(10f);

        itemCounter++;

        return table;

        // add barcode to document
        // document.add(codeEAN.createImageWithBarcode(cb, null, null));
    }

    int itemCounter = 0;
    Map items = new HashMap<>();
    List itemkeys;

    public void createPDFFiles() throws IOException, DocumentException {

        for (int customercount = 1; customercount < 10; customercount++) {

            String customer = new DecimalFormat("000").format(customercount);
            String path = resultpath + customer + "\\";
            if (!items.isEmpty()) {
                items.clear();
            }
            //items = finder.getAllItems();
            itemkeys = new ArrayList<>(items.keySet());
            itemCounter = 0;
            Collections.sort(itemkeys);

            createPdf(path, customercount);
        }
    }

}
