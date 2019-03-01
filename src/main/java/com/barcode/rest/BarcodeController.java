package com.barcode.rest;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class BarcodeController {

	@PostMapping("/upload/barcode")
	public ResponseEntity<?> uploadBarcode(@RequestPart("images") MultipartFile images, HttpServletResponse response) {
		
		String message = "";
		
		
		
		return new ResponseEntity<String>(message, HttpStatus.OK);
	}
	
}
