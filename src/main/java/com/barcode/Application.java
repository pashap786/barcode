package com.barcode;


import javax.servlet.MultipartConfigElement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import com.barcode.decoder.BarcodeImageDecoder;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public StandardServletMultipartResolver  standardServletMultipartResolver () {
	    return new StandardServletMultipartResolver ();
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
	    MultipartConfigFactory factory = new MultipartConfigFactory();
	    factory.setMaxRequestSize(DataSize.ofMegabytes(50));
	    factory.setMaxFileSize(DataSize.ofKilobytes(50));
	    return factory.createMultipartConfig();
	}

	@Bean
	public BarcodeImageDecoder barcodeImageDecoder() {
		return new BarcodeImageDecoder();
	}
}
