package de.obfusco.secondhand.receipt.file;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
@Transactional
public class ReceiptFile {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EventRepository eventRepository;

    public File createFile(Path basePath, String title, String introText) throws DocumentException, IOException {
        return createFile(basePath, title, introText, null, null);
    }

    protected void addHeader(Document document, String title) throws DocumentException {
        document.add(new Phrase(new Chunk(title, FontFactory
                .getFont(FontFactory.HELVETICA_BOLD, 24))));
    }

    public File createFile(Path basePath, String title, String introText, String payoutTitle, Map<Integer, String> payouts) throws IOException, DocumentException {
        Files.createDirectories(basePath);
        Path fullPath = Paths.get(basePath.toString(), "receipt.pdf");
        Document document = new Document(PageSize.A4, 80, 50, 50, 30);

        PdfWriter.getInstance(document,
                new FileOutputStream(fullPath.toFile()));

        document.open();
        addHeader(document, title);

        Iterable<Reservation> reservations = reservationRepository.findAllByOrderByNumberAsc();

        String[] columnNames = ((payoutTitle != null) ? new String[]{"ResNr", "Name", payoutTitle, "Unterschrift"} : new String[]{"ResNr", "Name", "Unterschrift"});
        float[] widths = (payoutTitle != null) ? new float[]{12f, 40f, 20f, 40f} : new float[]{12f, 40f, 40f};
        PdfPTable table = new PdfPTable(columnNames.length);
        table.setWidths(widths);
        table.setWidthPercentage(100f);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell headerCell = new PdfPCell(new Phrase(introText,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        headerCell.setBorder(0);
        headerCell.setPaddingBottom(20f);
        headerCell.setColspan(columnNames.length);
        table.addCell(headerCell);

        for (String columnName : columnNames) {
            table.addCell(new PdfPCell(new Phrase(new Chunk(columnName,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)))));
        }

        table.setHeaderRows(2);

        for (final Reservation reservation : reservations) {
            PdfPCell cell = new PdfPCell(new Phrase(new Chunk(Integer.toString(reservation.number),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14))));
            table.addCell(cell);
            cell = new PdfPCell(new Phrase(
                    reservation.seller.lastName
                            + ", "
                            + reservation.seller.firstName));
            table.addCell(cell);
            if (payoutTitle != null) {
                PdfPCell payoutCell = new PdfPCell(new Phrase(new Chunk(payouts.get(reservation.number),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14))));
                payoutCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(payoutCell);
            }
            cell = new PdfPCell();
            table.addCell(cell);
        }

        document.add(table);
        document.close();
        return fullPath.toFile();
    }
}
