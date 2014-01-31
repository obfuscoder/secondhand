package de.obfusco.secondhand.barcodefilegenerator;

import java.io.File;
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
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import de.obfusco.secondhand.common.constants.Constants;
import de.obfusco.secondhand.common.items.reader.CsvFinder;

public class BarCodeLabelSheet {

    /**
     * The resulting PDF.
     */
    private static final String RESULT = "C:\\flohmarkt\\resultLabel.pdf";
    private static final String resultpath = "C:\\flohmarkt\\completion\\";

    @SuppressWarnings("unchecked")
    public BarCodeLabelSheet() {

    }

    public String createPdf(String path, int customer) throws IOException,
            DocumentException {

        Document document = new Document(PageSize.A4, 0, 0, 40, 40);

        String filename = path + "resultLabel.pdf";
        PdfWriter writer = PdfWriter.getInstance(document,
                new FileOutputStream(filename));
        document.open();

        int size = items.size();
        int modItems = size % 3;
        int tablelinecounter;
        tablelinecounter = size / 3;
        if (modItems > 0) {
            tablelinecounter++;
        }
        for (int j = 1; j <= tablelinecounter; j++) {
            int tablecounter = 3;
            if (j == tablelinecounter && modItems > 0) {
                tablecounter = modItems;
            }

            createTableLine(document, writer, tablecounter, customer);

            if (j % 5 == 0) {
                document.newPage();
            }
        }

        document.close();

        return filename;
    }

    private void createTableLine(Document document, PdfWriter writer, int count, int customer)
            throws DocumentException {

        PdfPTable table = new PdfPTable(3); // 3 columns.

        table.setWidthPercentage(100);

        PdfPCell cell1 = new PdfPCell(new Paragraph(""));
        PdfPCell cell2 = new PdfPCell(new Paragraph(""));
        PdfPCell cell3 = new PdfPCell(new Paragraph(""));

        cell1.setBorderColor(BaseColor.WHITE);
        cell2.setBorderColor(BaseColor.WHITE);
        cell3.setBorderColor(BaseColor.WHITE);
        cell1.addElement(createTable(writer, document, customer));
        if (count > 1) {
            cell2.addElement(createTable(writer, document, customer));
        }
        if (count > 2) {
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

//		table.setSpacingBefore(5f);
        table.setTotalWidth(180);
        table.setLockedWidth(true);

        PdfPCell cell;

        String nr = (String) itemdata[0];
        cell = new PdfPCell(new Phrase(new Chunk(Integer.toString(customer), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18))));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk(nr.substring(5, 7), FontFactory.getFont(FontFactory.HELVETICA, 12))));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setColspan(2);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Bez.:"));
//		cell.setBorderColor(BaseColor.WHITE);
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(new Chunk((String) itemdata[2], FontFactory.getFont(FontFactory.HELVETICA, 10))));
        cell.setColspan(2);
        cell.setRowspan(3);
        cell.setBorderColor(BaseColor.WHITE);
        cell.setBorder(3);
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
        cell = new PdfPCell(new Phrase((String) itemdata[3]));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(""));
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase("Preis:"));
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);
        String s = (String) itemdata[4];
        Float floatprise = Float.parseFloat(s.replace(",", "."));
        String prise = String.format("%.2f", floatprise);
        cell = new PdfPCell(new Phrase(prise));
        cell.setBorderColor(BaseColor.WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Euro"));
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        Image barcode = codeEAN.createImageWithBarcode(cb, null, null);
        barcode.scalePercent(120f);
        cell = new PdfPCell(barcode);
        cell.setColspan(3);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(2);
//		cell.setPaddingLeft(50);
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);

        table.setSpacingAfter(2f);

        itemCounter++;

        return table;

        // add barcode to document
        // document.add(codeEAN.createImageWithBarcode(cb, null, null));
    }

    int itemCounter = 0;
    Map items = new HashMap<>();
    List itemkeys;

    public void createPDFFiles() throws IOException, DocumentException {

        for (int customercount = 1; customercount < Constants.CUSTOMER_COUNT; customercount++) {
            CsvFinder finder;

            String customer = new DecimalFormat("000").format(customercount);
            String path = resultpath + customer + "\\";
            finder = new CsvFinder(path + customer + "_userItems.csv");
            if (!items.isEmpty()) {
                items.clear();
            }
            items = finder.getAllItems();
            itemkeys = new ArrayList<>(items.keySet());
            itemCounter = 0;
            Collections.sort(itemkeys);

            createPdf(path, customercount);
        }
    }

    public String createPDFFile(int customernr) throws IOException, DocumentException {
        CsvFinder finder;

        String customer = new DecimalFormat("000").format(customernr);
        String path = resultpath + customer + "\\";
        (new File(path)).mkdirs();

        finder = new CsvFinder(path + "items.csv");
        if (!items.isEmpty()) {
            items.clear();
        }
        items = finder.getAllCustomerItems(customer);
        itemkeys = new ArrayList<>(items.keySet());
        itemCounter = 0;
        Collections.sort(itemkeys);

        String filename = createPdf(path, customernr);

        return filename;
    }

}
