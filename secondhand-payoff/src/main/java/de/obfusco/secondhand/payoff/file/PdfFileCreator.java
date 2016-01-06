package de.obfusco.secondhand.payoff.file;

import com.itextpdf.text.DocumentException;

import java.io.File;
import java.io.IOException;

public interface PdfFileCreator {

    File create() throws DocumentException, IOException;
}
