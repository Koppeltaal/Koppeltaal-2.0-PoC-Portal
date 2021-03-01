package nl.koppeltaal.poc.fhir.dto;

/**
 *
 */
public class LocationDto extends BaseDto {
	String address;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
