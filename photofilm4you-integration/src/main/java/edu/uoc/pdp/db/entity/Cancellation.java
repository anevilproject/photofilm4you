package edu.uoc.pdp.db.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="cancellation")
public class Cancellation implements Serializable, Identifiable {

	private static final long serialVersionUID = -4107081740684245627L;

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	private String id;
	private LocalDateTime creationDate;
	private BigDecimal penalization;
	@Enumerated(EnumType.STRING)
	private PenalizationStatus status;
	@OneToOne(mappedBy = "cancellation")
	private Rent rent;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public BigDecimal getPenalization() {
		return penalization;
	}

	public void setPenalization(BigDecimal penalization) {
		this.penalization = penalization;
	}

	public Rent getRent() {
		return rent;
	}

	public void setRent(Rent rent) {
		this.rent = rent;
	}

	public PenalizationStatus getStatus() {
		return status;
	}

	public void setStatus(PenalizationStatus status) {
		this.status = status;
	}
}
