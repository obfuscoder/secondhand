package de.obfusco.secondhand.barcodefilegenerator;

import de.obfusco.secondhand.storage.StorageConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.swing.*;

@Configuration
@ComponentScan("de.obfusco.secondhand")
@Import(StorageConfiguration.class)
public class BarCodeGenerator {

    private final static Logger LOG = LoggerFactory.getLogger(BarCodeGenerator.class);

    public static void main(String args[]) {
        LOG.info("starting application");
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(BarCodeGenerator.class);
        BarCodeGeneratorGui gui = applicationContext.getBean(BarCodeGeneratorGui.class);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setVisible(true);
    }
}
