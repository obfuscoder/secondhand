package de.obfusco.secondhand.barcodefilegenerator;

import com.itextpdf.text.DocumentException;
import de.obfusco.secondhand.storage.model.Event;
import de.obfusco.secondhand.storage.repository.EventRepository;
import de.obfusco.secondhand.storage.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class BarCodeGeneratorGui extends JFrame implements ActionListener {

    private final static Logger LOG = LoggerFactory.getLogger(BarCodeGeneratorGui.class);
    public JButton justBarcode;
    public JButton etiquettes;
    public JLabel customerNrLabel;
    public JTextField customerNr;
    public JLabel etiquettesLabel;
    public JLabel etiquettesLink;
    Path basePath = Paths.get("data/pdfs/labels");
    Path targetPath;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private BarCodeSheet barCodeSheet;
    @Autowired
    private BarCodeLabelSheet barCodeLabelSheet;

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

        customerNrLabel = new JLabel("Verkäufer:");
        customerNr = new JTextField();

        justBarcode = new JButton("Nur Barcodes");
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
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().open(targetPath.toFile());
                } catch (IOException e1) {
                    LOG.error("Failed to open file " + targetPath.toString(), e1);
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
        final Event event = eventRepository.find();
        if (actionEvent.getSource() == justBarcode) {
            try {
                targetPath = barCodeSheet.createPDFFile(basePath,
                        reservationRepository.findByNumber(Integer.parseInt(customerNr.getText()))
                );

                etiquettesLabel.setText("Barcodes für " + customerNr.getText());

            } catch (NumberFormatException | IOException | DocumentException e) {
                LOG.error("Failed to create bar codes file", e);
            }
        } else if (actionEvent.getSource() == etiquettes) {
            try {

                targetPath = barCodeLabelSheet.createPDFFile(basePath,
                        reservationRepository.findByNumber(Integer.parseInt(customerNr.getText())));

                etiquettesLabel.setText("Etiketten für " + customerNr.getText());
            } catch (DocumentException | IOException e) {
                LOG.error("Failed to create labels file", e);
            }
        }

        etiquettesLink.setText(targetPath.toString());
    }
}
