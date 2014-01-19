package com.barcodefilegenerator.createFile;

import java.io.IOException;

import com.itextpdf.text.DocumentException;

public class BuildFile {

	public static void main(String[] args) throws IOException,
	DocumentException {

//		new BarcodePDFCreator().createPDFFiles();
//		new BARCodeSheet().createPDFFiles();
		new BarCodeLabelSheet().createPDFFiles();
	}
}
