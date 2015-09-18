package de.obfusco.secondhand.gui.config;

import de.obfusco.secondhand.net.*;
import de.obfusco.secondhand.net.dto.Event;
import de.obfusco.secondhand.storage.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

@Component
public class ConfigGui extends JDialog {
    private JPanel contentPane;
    private JButton downloadButton;
    private JTextField rootUrlField;
    private JTextField tokenField;
    private JButton pushDataButton;
    private JButton uploadButton;

    @Autowired
    StorageConverter storageConverter;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    SellerRepository sellerRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    private DataPusher dataPusher;
    @Autowired
    private TransactionUploader transactionUploader;

    public ConfigGui() {
        setTitle("Einstellungen");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(downloadButton);
        pack();
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {
                de.obfusco.secondhand.storage.model.Event event = eventRepository.find();
                if (event != null) {
                    tokenField.setText(event.token);
                }
            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) {
            }

            @Override
            public void windowIconified(WindowEvent windowEvent) {
            }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) {
            }

            @Override
            public void windowActivated(WindowEvent windowEvent) {
            }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) {
            }
        });
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                download();
            }
        });

        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                upload();
            }
        });

        pushDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                push();
            }
        });
    }

    private void download() {
        try {
            int dialogOptions = JOptionPane.YES_NO_OPTION;
            String question = "Es werden sämtliche Daten in der Datenbank gelöscht und\n" +
                    "mit den Daten aus dem Online-System überschrieben.\n" +
                    "Sollten lokal bereits Verkäufe getätigt worden sein,\ndie noch nicht zum Online-System " +
                    "synchronisiert wurden,\ngehen diese verloren.\n\n" +
                    "Sind Sie sicher?";
            int dialogResult = JOptionPane.showConfirmDialog(null, question, "Achtung", dialogOptions);
            if (dialogResult == JOptionPane.YES_OPTION) {
                downloadDatabase(rootUrlField.getText(), tokenField.getText());
                reportSuccess();
            }
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(null,
                    "Angabe der Homepage nicht korrekt. Bitte korrigieren Sie Ihre Eingabe",
                    "Eingabefehler",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Fehler beim Herunterladen der Daten. Bitte prüfen Sie die Internetverbindung und Eingabe.",
                    "Downloadfehler",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void push() {
        int dialogOptions = JOptionPane.YES_NO_OPTION;
        String question = "Es werden sämtliche Daten in der Datenbank der anderen Kassensysteme gelöscht\n" +
                "und mit den Daten dieses Kassensystems überschrieben.\n\n" +
                "Sind Sie sicher?";
        int dialogResult = JOptionPane.showConfirmDialog(null, question, "Achtung", dialogOptions);
        if (dialogResult == JOptionPane.YES_OPTION) {
            pushDatabase();
        }
    }

    private void upload() {
        try {
            transactionUploader.upload(rootUrlField.getText(), tokenField.getText());
            String message = String.format("%d Transaktionen für den Termin \"%s\" erfolgreich hochgeladen.",
                    transactionRepository.count(), eventRepository.find().name);
            JOptionPane.showMessageDialog(this, message, "Upload erfolgreich", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Fehler beim Hochladen der Daten. Bitte prüfen Sie die Internetverbindung und Eingabe.",
                    "Uploadfehler",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void pushDatabase() {
        Event event = storageConverter.convertToEvent();
        dataPusher.push(event);
    }

    private void reportSuccess() {
        rootUrlField.getText();
        String message = String.format("Daten für den Termin \"%s\" erfolgreich importiert.\nVerkäufer: %d, Artikel: %d",
                eventRepository.find().name, sellerRepository.count(), itemRepository.count());
        JOptionPane.showMessageDialog(this, message, "Import erfolgreich", JOptionPane.INFORMATION_MESSAGE);
    }

    private void downloadDatabase(String baseUrl, String token) throws IOException {
        InputStream inputStream = new EventDownloader().downloadEventData(baseUrl, token);
        Event event = new JsonEventConverter().parse(inputStream);
        event.token = token;
        storageConverter.storeEvent(event);
    }

    public void setRootUrl(String rootUrl) {
        rootUrlField.setText(rootUrl);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(400, -1), null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Datenaustausch mit Online-System"));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Homepage");
        panel2.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rootUrlField = new JTextField();
        rootUrlField.setText("flohmarkthelfer.de");
        panel2.add(rootUrlField, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Token");
        panel2.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tokenField = new JTextField();
        panel2.add(tokenField, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        downloadButton = new JButton();
        downloadButton.setText("Daten herunterladen");
        panel1.add(downloadButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        uploadButton = new JButton();
        uploadButton.setText("Transaktionen hochladen");
        panel1.add(uploadButton, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Datenaustausch mit anderen Kassensystemen"));
        pushDataButton = new JButton();
        pushDataButton.setText("Daten zu anderen Kassensystemen synchronisieren");
        panel3.add(pushDataButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        contentPane.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
