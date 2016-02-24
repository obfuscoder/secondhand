package de.obfusco.secondhand.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;

public class Main {

    private final static Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {
        setupUncaughtExceptionHandler();
        LOG.info("Starting application");
        try {
            ApplicationContext context = new AnnotationConfigApplicationContext(MainConfiguration.class);
            MainGui mainGui = context.getBean(MainGui.class);
            mainGui.start();
        } catch (Exception ex) {
            LOG.error("Error while starting application!", ex);
            JOptionPane.showMessageDialog(null, "Fehler beim Initialisieren der Applikation.\n" +
                            "Mögliche Ursache ist eine defekte, blockierte oder nicht vorhandene Datenbank.\n" +
                            "Haben Sie die Applikation vielleicht bereits gestartet?\n" +
                            "Details zum Fehler finden Sie in der Protokolldatei.",
                    "Programmfehler",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String args[]) {
        new Main();
    }

    private void setupUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new LoggingExceptionHandler());
        System.setProperty("sun.awt.exception.handler", LoggingExceptionHandler.class.getName());
    }
}
