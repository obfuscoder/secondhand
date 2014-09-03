package de.obfusco.secondhand.gui;

import de.obfusco.secondhand.net.MessageBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;

public class Main implements MessageBroker {

    private Main() {
        LOG.info("Starting application");
        try {
            new AnnotationConfigApplicationContext(MainConfiguration.class);
        } catch (Exception ex) {
            LOG.error("Error while starting application!", ex);
            JOptionPane.showMessageDialog(null, "Fehler beim Initialisieren der Applikation.\n" +
                            "Mögliche Ursache ist eine defekte oder nicht vorhandene Datenbank.\n" +
                            "Details finden Sie in der Protokolldatei.",
                    "Programmfehler",
                    JOptionPane.ERROR_MESSAGE);
                return;
        }
    }

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String args[]) {
        new Main();
    }

    @Override
    public String message(String requestMessage) {
        return null;
    }
}
