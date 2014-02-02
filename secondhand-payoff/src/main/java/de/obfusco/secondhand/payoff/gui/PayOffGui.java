package de.obfusco.secondhand.payoff.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.itextpdf.text.DocumentException;

import de.obfusco.secondhand.payoff.file.SellerPayOff;
import de.obfusco.secondhand.payoff.file.TotalPayOff;
import de.obfusco.secondhand.storage.model.Reservation;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PayOffGui extends JFrame {

    public static final int EVENT_ID = 1;

    public JLabel totalPayoff;

    private String resultpath = "C:\\flohmarkt\\Abrechnung\\";

    private static final String TITLE = "Abrechnung";

    @Autowired
    TotalPayOff totalPayOff;

    @Autowired
    SellerPayOff sellerPayOff;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EventRepository eventRepository;

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

        totalPayoff = new JLabel("Gesamtübersicht");
        totalPayoff.setFont(title.getFont().deriveFont(20.0f));
        totalPayoff.setForeground(Color.BLUE.darker());
        totalPayoff.setCursor(Cursor
                .getPredefinedCursor(Cursor.HAND_CURSOR));

        totalPayoff.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().open(totalPayOff.createTotalPayoffFile(resultpath,
                            eventRepository.findOne(EVENT_ID)));
                } catch (DocumentException | IOException ex) {
                    Logger.getLogger(PayOffGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        pane.add(totalPayoff);
        pane.add(new JSeparator(JSeparator.HORIZONTAL));

        JPanel panel = new JPanel();
        pane.add(new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
        panel.setLayout(new GridLayout(0, 2, 10, 10));

        List<Reservation> reservations = reservationRepository.findByEvent(eventRepository.findOne(EVENT_ID));

        for (final Reservation reservation : reservations) {

            JLabel customerPayoffNr = new JLabel(reservation.getNumber() + " | " + reservation.getSeller().getName());

            customerPayoffNr.setFont(title.getFont().deriveFont(14.0f));
            customerPayoffNr.setForeground(Color.BLUE.darker());
            customerPayoffNr.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            customerPayoffNr.addMouseListener(new MouseListener() {

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
                        Desktop.getDesktop().open(sellerPayOff.createFile(resultpath, reservation));
                    } catch (DocumentException | IOException ex) {
                        Logger.getLogger(PayOffGui.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            panel.add(customerPayoffNr);
        }
    }
}
