package com.barcode.service;

import com.barcode.entity.PatientId;

public interface PatientIdService {
	
	public PatientId findPatientId(String username);
	public PatientId generatePatientId(String username);

}
