package com.barcode.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.barcode.component.BarcodeInfo;
import com.barcode.component.WebFileWrapperImplementation;
import com.barcode.decoder.BarcodeImageDecoder;
import com.barcode.decoder.BarcodeImageDecoder.BarcodeDecodingException;
import com.barcode.imageutil.ImageUtility;

@RestController
public class BarcodeController {

	private Logger log = LoggerFactory.getLogger(BarcodeController.class);
	
	@Autowired
	private BarcodeImageDecoder bd;

	@PostMapping("/upload/barcode")
	public ResponseEntity<?> uploadBarcode(@RequestPart("images") MultipartFile images, HttpServletResponse response)
			throws FileNotFoundException {

		WebFileWrapperImplementation file = new WebFileWrapperImplementation(images);
		try {
			if (images.getName().contains(".png")) {
				log.info("CONVERTING FROM PNG");
				File f = ImageUtility.pngToJpg(file);
				FileInputStream is = new FileInputStream(f);
				BarcodeInfo bi = bd.decodeImage(is);
				return new ResponseEntity<>(bi, HttpStatus.OK);
			} else {
				log.info("Not converting");
				BarcodeInfo bi = bd.decodeImage(file.getInputStream());
				return new ResponseEntity<>(bi, HttpStatus.OK);
			}
		} catch (BarcodeDecodingException e) {
			e.printStackTrace();
			return new ResponseEntity<String>("BARCODE FAILED TO PARSE", HttpStatus.OK);
		}

	}

}
