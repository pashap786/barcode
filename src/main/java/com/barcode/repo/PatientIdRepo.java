package com.barcode.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.barcode.entity.PatientId;

public interface PatientIdRepo extends JpaRepository<PatientId, Long> {
	
	@Query("select p from Patient p where p.username=:username")
	public PatientId findPatientIdByUsername(@Param("username") String username);

}
