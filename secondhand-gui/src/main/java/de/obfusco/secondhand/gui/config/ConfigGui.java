package de.obfusco.secondhand.gui.config;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import de.obfusco.secondhand.net.*;
import de.obfusco.secondhand.net.dto.Event;
import de.obfusco.secondhand.storage.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

@Component
public class ConfigGui extends JDialog {
    private final static Logger LOG = LoggerFactory.getLogger(ConfigGui.class);

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
    private JPanel contentPane;
    private JButton downloadButton;
    private JTextField tokenField;
    private JButton pushDataButton;
    private JButton uploadButton;
    private JButton exportDataButton;
    @Autowired
    private DataPusher dataPusher;
    @Autowired
    private TransactionUploader transactionUploader;
    private String rootUrl;

    private ConfigGui() {
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
        downloadButton.addActionListener(actionEvent -> download());

        uploadButton.addActionListener(actionEvent -> upload());

        pushDataButton.addActionListener(actionEvent -> push());
        exportDataButton.addActionListener(actionEvent -> export());
    }

    private void export() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG | JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Bitte geben Sie das Verzeichnis an, in das die Daten exportiert werden sollen");
        if (fileChooser.showDialog(this, "Exportieren") != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File exportPath = fileChooser.getSelectedFile();
        File exportFile = new File(exportPath, "flohmarkthelfer.data");
        Event event = storageConverter.convertToEvent();
        JsonDataConverter converter = new JsonDataConverter();
        try (FileOutputStream fileOutputStream = new FileOutputStream(exportFile)) {
            converter.writeCompressedJsonToStream(event, fileOutputStream);
            JOptionPane.showMessageDialog(null,
                    "Die Daten wurden erfolgreich exportiert.",
                    "Export erfolgreich",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            LOG.error("Error while exporting database", e);
            JOptionPane.showMessageDialog(null,
                    "Fehler beim Exportieren der Daten. Bitte wählen Sie ein Verzeichnis, in das geschrieben werden kann",
                    "Exportfehler",
                    JOptionPane.WARNING_MESSAGE);
        }
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
                downloadDatabase(rootUrl, tokenField.getText());
                reportSuccess();
            }
        } catch (MalformedURLException e) {
            LOG.error("Error while downloading database", e);
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
            try {
                pushDatabase();
            } catch (IOException e) {
                LOG.error("Error while pushing database", e);
                JOptionPane.showMessageDialog(null,
                        "Fehler beim Übertragen der Daten.",
                        "Übertragungsfehler",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void upload() {
        String question = "Die aktuellen Transaktionen werden zum Online-System hochgeladen.\n\n" +
                "Wollen Sie fortfahren?";
        int dialogResult = JOptionPane.showConfirmDialog(null, question, "Frage", JOptionPane.YES_NO_OPTION);
        if (dialogResult != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            boolean success = transactionUploader.upload(rootUrl, tokenField.getText());
            if (success) {
                String message = String.format("%d Transaktionen für den Termin \"%s\" erfolgreich hochgeladen.",
                        transactionRepository.count(), eventRepository.find().name);
                JOptionPane.showMessageDialog(this, message, "Upload erfolgreich", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        } catch (IOException e) {
            LOG.error("Error while uploading transactions", e);
        }
        JOptionPane.showMessageDialog(null,
                "Fehler beim Hochladen der Daten. Bitte prüfen Sie die Internetverbindung und Eingabe.",
                "Uploadfehler",
                JOptionPane.WARNING_MESSAGE);
    }

    private void pushDatabase() throws IOException {
        Event event = storageConverter.convertToEvent();
        dataPusher.push(event);
    }

    private void reportSuccess() {
        String message = String.format("Daten für den Termin \"%s\" erfolgreich importiert.\nVerkäufer: %d, Artikel: %d",
                eventRepository.find().name, sellerRepository.count(), itemRepository.count());
        JOptionPane.showMessageDialog(this, message, "Import erfolgreich", JOptionPane.INFORMATION_MESSAGE);
    }

    private void downloadDatabase(String baseUrl, String token) throws IOException {
        InputStream inputStream = new EventDownloader().downloadEventData(baseUrl, token);
        Event event = new JsonDataConverter().parse(inputStream);
        storageConverter.storeEvent(event);
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
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(400, -1), null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Datenaustausch mit Online-System"));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Homepage");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Token");
        panel2.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tokenField = new JTextField();
        panel2.add(tokenField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        downloadButton = new JButton();
        downloadButton.setText("Daten herunterladen");
        panel1.add(downloadButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        uploadButton = new JButton();
        uploadButton.setText("Transaktionen hochladen");
        panel1.add(uploadButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Datenaustausch mit anderen Kassensystemen"));
        pushDataButton = new JButton();
        pushDataButton.setText("Daten zu anderen Kassensystemen synchronisieren");
        panel3.add(pushDataButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        exportDataButton = new JButton();
        exportDataButton.setText("Daten exportieren");
        panel3.add(exportDataButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        contentPane.add(spacer1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }
}
