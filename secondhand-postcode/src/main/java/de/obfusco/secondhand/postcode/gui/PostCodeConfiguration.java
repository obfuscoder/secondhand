package de.obfusco.secondhand.postcode.gui;

import de.obfusco.secondhand.storage.StorageConfiguration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("de.obfusco.secondhand")
@Import(StorageConfiguration.class)
public class PostCodeConfiguration {

}
