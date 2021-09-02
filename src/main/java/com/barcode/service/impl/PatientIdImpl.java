package com.barcode.service.impl;

import java.awt.image.BufferedImage;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.barcode.entity.PatientId;
import com.barcode.imageutil.QrCodeUtil;
import com.barcode.repo.PatientIdRepo;
import com.barcode.service.PatientIdService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class PatientIdImpl implements PatientIdService {
	
	private final PatientIdRepo patientIdRepo;
	
	@Override
	public PatientId findPatientId(String username) {
		log.info("find patient id ={}", username);
		return patientIdRepo.findPatientIdByUsername(username);
	}

	@Override
	public PatientId generatePatientId(String username) {
		log.info("generatePatientId  ={}", username);
		try {
			BufferedImage qrImage = QrCodeUtil.generateQRCodeImage(this.generateUUIDForPatient());
			String filepath = QrCodeUtil.writeQrCodeImage(qrImage);
			PatientId patientId = new PatientId();
			patientId.setUsername(username);
			patientId.setPatientQrImageFilePath(filepath);
			patientIdRepo.save(patientId);
		} catch(Exception e) {
			e.printStackTrace();
			log.error("Error generatePatientId {}", e.getMessage());
		}
		return null;
	}
	
	private String generateUUIDForPatient() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

}
