package de.obfusco.secondhand.postcode;

import javax.swing.JFrame;

import de.obfusco.secondhand.postcode.gui.PostCodeGui;
import de.obfusco.secondhand.storage.StorageConfiguration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("de.obfusco.secondhand")
@Import(StorageConfiguration.class)
public class PostCode {

    public static void main(String args[]) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(PostCode.class);
        PostCodeGui gui = applicationContext.getBean(PostCodeGui.class);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setVisible(true);
    }
}
