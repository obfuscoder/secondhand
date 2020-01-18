package de.obfusco.secondhand.sale;

import de.obfusco.secondhand.sale.gui.SaleDialog;
import de.obfusco.secondhand.storage.StorageConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.swing.*;

@Configuration
@ComponentScan("de.obfusco.secondhand")
@Import(StorageConfiguration.class)
public class Sale {

    public static void main(String args[]) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Sale.class);
        SaleDialog gui = applicationContext.getBean(SaleDialog.class);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setVisible(true);

    }
}
