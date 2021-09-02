package com.barcode.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDto {

	private String name;
	private Timestamp dateCreated;
	
	
}
