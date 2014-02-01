package de.obfusco.secondhand.sale;

import javax.swing.JFrame;

import de.obfusco.secondhand.sale.gui.CashBoxGui;
import de.obfusco.secondhand.storage.StorageConfiguration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("de.obfusco.secondhand")
@Import(StorageConfiguration.class)
public class CashBox {

    public static void main(String args[]) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(CashBox.class);
        CashBoxGui gui = applicationContext.getBean(CashBoxGui.class);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setVisible(true);

    }
}
