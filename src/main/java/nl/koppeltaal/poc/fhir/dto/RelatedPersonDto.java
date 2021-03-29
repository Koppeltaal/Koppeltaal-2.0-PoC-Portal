package nl.koppeltaal.poc.fhir.dto;

/**
 *
 */
public class RelatedPersonDto extends PersonDto {
	String email;
	String phone;
	String addressLines;
	String addressCity;
	String addressPostalCode;
	String addressCountry;
	String patient;
	/**
	 * Format relationshipSystem|relationshipCode
	 */
	String relationship;
	String relationshipSystem;
	String relationshipCode;

	public String getAddressCity() {
		return addressCity;
	}

	public void setAddressCity(String addressCity) {
		this.addressCity = addressCity;
	}

	public String getAddressCountry() {
		return addressCountry;
	}

	public void setAddressCountry(String addressCountry) {
		this.addressCountry = addressCountry;
	}

	public String getAddressLines() {
		return addressLines;
	}

	public void setAddressLines(String addressLines) {
		this.addressLines = addressLines;
	}

	public String getAddressPostalCode() {
		return addressPostalCode;
	}

	public void setAddressPostalCode(String addressPostalCode) {
		this.addressPostalCode = addressPostalCode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPatient() {
		return patient;
	}

	public void setPatient(String patient) {
		this.patient = patient;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getRelationshipCode() {
		return relationshipCode;
	}

	public void setRelationshipCode(String relationshipCode) {
		this.relationshipCode = relationshipCode;
	}

	public String getRelationshipSystem() {
		return relationshipSystem;
	}

	public void setRelationshipSystem(String relationshipSystem) {
		this.relationshipSystem = relationshipSystem;
	}

}
