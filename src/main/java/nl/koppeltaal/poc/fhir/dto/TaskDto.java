package nl.koppeltaal.poc.fhir.dto;

/**
 *
 */
public class TaskDto extends BaseDto {
	String activityDefinition;
	String patient;
	String practitioner;
	String status;

	public String getActivityDefinition() {
		return activityDefinition;
	}

	public void setActivityDefinition(String activityDefinition) {
		this.activityDefinition = activityDefinition;
	}

	public String getPatient() {
		return patient;
	}

	public void setPatient(String patient) {
		this.patient = patient;
	}

	public String getPractitioner() {
		return practitioner;
	}

	public void setPractitioner(String practitioner) {
		this.practitioner = practitioner;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
