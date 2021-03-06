package de.obfusco.secondhand.payoff.gui;

import com.itextpdf.text.DocumentException;
import de.obfusco.secondhand.payoff.file.PdfFileCreator;
import de.obfusco.secondhand.payoff.file.SellerPayOff;
import de.obfusco.secondhand.payoff.file.TotalPayOff;
import de.obfusco.secondhand.storage.model.Event;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PayOffGui extends JFrame {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(PayOffGui.class);
    private static final String TITLE = "Abrechnung";
    private JLabel totalPayoff;
    @Autowired
    TotalPayOff totalPayOff;

    @Autowired
    SellerPayOff sellerPayOff;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EventRepository eventRepository;

    private Path basePath = Paths.get("data/pdfs/payoff");

    public PayOffGui() {
        super(TITLE);
        setSize(500, 600);
        setLocationRelativeTo(null);
    }

    public void open() {
        final Container pane = getContentPane();
        pane.removeAll();
        addComponentsToPane(pane);
        setVisible(true);
    }

    private void addComponentsToPane(Container pane) {
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(TITLE);
        title.setFont(title.getFont().deriveFont(30.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        pane.add(title);
        JLabel hint = new JLabel("Klicken Sie bitte auf den jeweiligen Eintrag, um die Abrechnung dafür zu erstellen und zu öffnen.");
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        pane.add(hint);

        final Event event = eventRepository.find();
        final PdfFileCreator totalPayoffCreator = () -> totalPayOff.createTotalPayoffFile(basePath, event);

        final Iterable<Reservation> reservations = reservationRepository.findAllByOrderByNumberAsc();

        final PdfFileCreator allSellerPayoffCreator = () -> sellerPayOff.createFileForAll(basePath, reservations);

        totalPayoff = createPdfLink("Gesamtübersicht", 20.0f, totalPayoffCreator);

        JLabel allPayOff = createPdfLink("Alle Verkäufer", 20.0f, allSellerPayoffCreator);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(0, 2, 10, 10));
        pane.add(topPanel);
        topPanel.add(totalPayoff);
        topPanel.add(allPayOff);
        pane.add(new JSeparator(JSeparator.HORIZONTAL));

        JPanel panel = new JPanel();
        pane.add(new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        panel.setLayout(new GridLayout(0, 2, 10, 10));

        for (final Reservation reservation : reservations) {

            final PdfFileCreator sellerPayoffCreator = () -> sellerPayOff.createFile(basePath, reservation);
            if (reservation == null) throw new RuntimeException("Shiit");
            if (reservation.seller == null) throw new RuntimeException(String.valueOf(reservation.number));
            JLabel customerPayoffNr = createPdfLink(
                    reservation.number + " | " + reservation.seller.getName(),
                    14.0f, sellerPayoffCreator);
            panel.add(customerPayoffNr);
        }
    }

    private JLabel createPdfLink(final String text, final float fontSize, final PdfFileCreator pdfCreator) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(fontSize));
        label.setForeground(Color.BLUE.darker());
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().open(pdfCreator.create());
                } catch (DocumentException | IOException ex) {
                    LOG.error("Could not create file", ex);
                }
            }
        });
        return label;
    }
}
