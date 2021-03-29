package nl.koppeltaal.poc.fhir.dto;

/**
 *
 */
public class OrgPersonDto extends PersonDto {
	String organization;

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}
}
