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
}
