package de.obfusco.secondhand.gui.config;

import de.obfusco.secondhand.net.DataPusher;
import de.obfusco.secondhand.net.EventDownloader;
import de.obfusco.secondhand.net.EventStorageConverter;
import de.obfusco.secondhand.net.JsonEventConverter;
import de.obfusco.secondhand.net.dto.Event;
import de.obfusco.secondhand.storage.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    @Autowired
    EventStorageConverter eventStorageConverter;

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
    private DataPusher dataPusher;

    public ConfigGui() {
        setTitle("Einstellungen");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(downloadButton);
        pack();
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
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
        });

        pushDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int dialogOptions = JOptionPane.YES_NO_OPTION;
                String question = "Es werden sämtliche Daten in der Datenbank der anderen Kassensysteme gelöscht\n" +
                        "und mit den Daten dieses Kassensystems überschrieben.\n\n" +
                        "Sind Sie sicher?";
                int dialogResult = JOptionPane.showConfirmDialog(null, question, "Achtung", dialogOptions);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    pushDatabase();
                }
            }
        });
    }

    private void pushDatabase() {
        Event event = eventStorageConverter.convertToEvent();
        dataPusher.push(event);
    }

    private void reportSuccess() {
        String message = String.format("Daten für den Termin \"%s\" erfolgreich importiert.\nVerkäufer: %d, Artikel: %d",
                eventRepository.find().name, sellerRepository.count(), itemRepository.count());
        JOptionPane.showMessageDialog(this, message, "Import erfolgreich", JOptionPane.INFORMATION_MESSAGE);
    }

    private void downloadDatabase(String baseUrl, String token) throws IOException {
        InputStream inputStream = new EventDownloader().downloadEventData(baseUrl, token);
        Event event = new JsonEventConverter().parse(inputStream);
        eventStorageConverter.storeEvent(event);
    }
}
