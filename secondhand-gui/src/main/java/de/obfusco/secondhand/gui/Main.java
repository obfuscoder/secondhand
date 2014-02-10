package de.obfusco.secondhand.gui;

import de.obfusco.secondhand.storage.StorageConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("de.obfusco.secondhand")
@Import(StorageConfiguration.class)
public class Main {

    public static void main(String args[]) {
        Logger logger = LoggerFactory.getLogger(Main.class);

        logger.info("Starting application");
        try {
            ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Main.class);
        } catch (Exception ex) {
            logger.error("Error while starting application!", ex);
        }
    }
}
