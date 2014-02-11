package de.obfusco.secondhand.payoff.file;

import java.io.File;
import java.io.IOException;

import com.itextpdf.text.DocumentException;

public interface PdfFileCreator {

    File create() throws DocumentException, IOException;
}
