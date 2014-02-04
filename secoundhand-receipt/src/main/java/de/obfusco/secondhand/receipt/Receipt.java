package de.obfusco.secondhand.receipt;

import java.io.FileNotFoundException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.itextpdf.text.DocumentException;

import de.obfusco.secondhand.receipt.file.ReceiptFile;
import de.obfusco.secondhand.storage.StorageConfiguration;

@Configuration
@ComponentScan("de.obfusco.secondhand")
@Import(StorageConfiguration.class)
public class Receipt {

	public static void main(String[] args) {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(
				Receipt.class);
		ReceiptFile receipt = applicationContext.getBean(ReceiptFile.class);
		try {
			receipt.createFile();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}