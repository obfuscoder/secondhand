package de.obfusco.secondhand.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import sun.org.mozilla.javascript.json.JsonParser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ConfigGui extends JDialog {
    private JPanel contentPane;
    private JButton downloadButton;
    private JTextField rootUrlField;
    private JTextField tokenField;

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
                    URL url = new URL(String.format("http://%s/api/event", rootUrlField.getText()));
                    downloadDatabase(url);
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
    }

    private void downloadDatabase(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String authorization = String.format("Token %s", tokenField.getText());
        connection.setRequestProperty("Authorization", authorization);
        InputStream response = connection.getInputStream();
        Gson gson = new GsonBuilder().create();
    }
}
