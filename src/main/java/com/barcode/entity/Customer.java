package com.barcode.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer")
public class Customer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1234588L;

	@Id
	@Column(name = "id")
	private Long id;
	@Column(name = "customer_name")
	private String customerName;
	@Column(name = "security_id")
	private Long security;
	@Column(name = "client_id")
	private String clientId;
	@Column(name = "client_secret")
	private String clientSecret;
	@Column(name = "date_created")
	private Timestamp dateCreated;
	@Column(name = "date_updated")
	private Timestamp dateUpdated;
}
