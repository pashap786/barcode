package com.barcode.rest;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.barcode.component.BarcodeInfo;
import com.barcode.component.WebFileWrapperImplementation;
import com.barcode.decoder.BarcodeImageDecoder;
import com.barcode.decoder.BarcodeImageDecoder.BarcodeDecodingException;

@RestController
public class BarcodeController {

	@Autowired
	private BarcodeImageDecoder bd;
	
	@GetMapping("/generate")
	public ResponseEntity<?> generateBarcode(@RequestParam("username") String username) {
		
		return null;
	}

	@PostMapping("/upload/barcode")
	public ResponseEntity<?> uploadBarcode(@RequestPart("images") MultipartFile images, HttpServletResponse response) {

		WebFileWrapperImplementation file = new WebFileWrapperImplementation(images);
		try {
			BarcodeInfo bi = bd.decodeImage(file.getInputStream());
			return new ResponseEntity<>(bi, HttpStatus.OK);
		} catch (BarcodeDecodingException e) {
			return new ResponseEntity<String>("BARCODE FAILED TO PARSE", HttpStatus.OK);
		}

	}

}
