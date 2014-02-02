package de.obfusco.secondhand.barcodefilegenerator;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.itextpdf.text.DocumentException;

import de.obfusco.secondhand.storage.model.Event;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BarCodeGeneratorGui extends JFrame implements ActionListener {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BarCodeSheet barCodeSheet;

    @Autowired
    private BarCodeLabelSheet barCodeLabelSheet;

    public JButton justBarcode;
    public JButton etiquettes;
    public JLabel customerNrLabel;
    public JTextField customerNr;

    public JLabel etiquettesLabel;
    public JLabel etiquettesLink;
    public String filename;

    public BarCodeGeneratorGui() {
        super("Etiketten/ Barcodes");
        setSize(1000, 800);
        addComponentsToPane(getContentPane());
        pack();
        setLocationRelativeTo(null);
    }

    private void addComponentsToPane(Container pane) {
        JLabel title = new JLabel("Etiketten/ Barcodes");
        title.setFont(title.getFont().deriveFont(30.0f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        pane.add(title, BorderLayout.NORTH);

        JPanel panel = new JPanel();

        customerNrLabel = new JLabel("Kundennr:");
        customerNr = new JTextField();

        justBarcode = new JButton("Nur BarCodes");
        justBarcode.addActionListener(this);
        etiquettes = new JButton("Etiketten erzeugen");
        etiquettes.addActionListener(this);

        etiquettesLabel = new JLabel();
        etiquettesLink = new JLabel();
        etiquettesLink
                .setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        etiquettesLink.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().open(new File(filename));
                } catch (IOException e1) {

                    e1.printStackTrace();
                }
            }
        });

        panel.setLayout(new GridLayout(3, 2));
        panel.add(customerNrLabel);
        panel.add(customerNr);
        panel.add(justBarcode);
        panel.add(etiquettes);
        panel.add(etiquettesLabel);
        panel.add(etiquettesLink);

        pane.add(panel, BorderLayout.CENTER);

    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        final Event event = eventRepository.findOne(1);
        if (actionEvent.getSource() == justBarcode) {
            try {
                filename = barCodeSheet.createPDFFile(
                        reservationRepository.findByEventAndNumber(
                                event,
                                Integer.parseInt(customerNr.getText()))
                );

                etiquettesLabel.setText("Barcodes für " + customerNr.getText());

            } catch (NumberFormatException | IOException | DocumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (actionEvent.getSource() == etiquettes) {
            try {

                filename = barCodeLabelSheet.createPDFFile(
                        reservationRepository.findByEventAndNumber(
                                event,
                                Integer.parseInt(customerNr.getText())));

                etiquettesLabel.setText("Etiketten für " + customerNr.getText());
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (DocumentException ex) {
                Logger.getLogger(BarCodeGeneratorGui.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        etiquettesLink.setText(filename);
    }
}
