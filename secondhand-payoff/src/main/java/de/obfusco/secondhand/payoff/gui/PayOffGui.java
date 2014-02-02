package de.obfusco.secondhand.payoff.gui;

import java.awt.BorderLayout;
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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.itextpdf.text.DocumentException;

import de.obfusco.secondhand.payoff.file.CustomerPayOff;
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
    public JLabel totalPayoffLink;

    private String resultpath = "C:\\flohmarkt\\Abrechnung\\";
    private static final String filename = "total_payoff.pdf";

    private static final String TITLE = "Abrechnung";

    @Autowired
    TotalPayOff totalPayOff;

    @Autowired
    CustomerPayOff customerPayOff;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    EventRepository eventRepository;

    public PayOffGui() {
        super(TITLE);
        setSize(800, 800);
        setLocationRelativeTo(null);
    }

    public void open() {
        final Container pane = getContentPane();
        pane.removeAll();
        addComponentsToPane(pane);
        setVisible(true);
    }

    private void addComponentsToPane(Container pane) {
        JLabel title = new JLabel(TITLE);
        title.setFont(title.getFont().deriveFont(30.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        pane.add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel();

        totalPayoff = new JLabel("Gesamtübersicht");
        totalPayoff.setFont(title.getFont().deriveFont(20.0f));
        totalPayoffLink = new JLabel(resultpath + filename);
        totalPayoffLink.setCursor(Cursor
                .getPredefinedCursor(Cursor.HAND_CURSOR));

        totalPayoffLink.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().open(totalPayOff.createTotalPayoffFile(
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
        List<Reservation> reservations = reservationRepository.findByEvent(eventRepository.findOne(EVENT_ID));
        panel.setLayout(new GridLayout(reservations.size() + 3, 2));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        panel.add(new JSeparator(JSeparator.HORIZONTAL));
        panel.add(new JSeparator(JSeparator.HORIZONTAL));

        panel.add(totalPayoff);
        panel.add(totalPayoffLink);

        panel.add(new JSeparator(JSeparator.HORIZONTAL));
        panel.add(new JSeparator(JSeparator.HORIZONTAL));

        for (final Reservation reservation : reservations) {

            JLabel customerPayoffNr = new JLabel(reservation.getNumber() + " | " + reservation.getSeller().getName());

            customerPayoffNr.setFont(title.getFont().deriveFont(14.0f));
            final String file_name = resultpath + reservation.getNumber() + "\\" + filename;
            JLabel payoffLink = new JLabel(file_name);
            payoffLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            payoffLink.addMouseListener(new MouseListener() {

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
                        Desktop.getDesktop().open(customerPayOff.createFile(reservation));
                    } catch (DocumentException | IOException ex) {
                        Logger.getLogger(PayOffGui.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            panel.add(customerPayoffNr);
            panel.add(payoffLink);
        }
        pane.add(new JScrollPane(panel), BorderLayout.CENTER);
    }
}
