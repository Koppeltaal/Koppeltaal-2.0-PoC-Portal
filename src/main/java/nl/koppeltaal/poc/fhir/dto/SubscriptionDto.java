package nl.koppeltaal.poc.fhir.dto;

import org.hl7.fhir.r4.model.Subscription;

/**
 *
 */
public class SubscriptionDto extends BaseDto {
	String id;
	String criteria;
	Subscription.SubscriptionChannelType type;
	Subscription.SubscriptionStatus status;
	String endpoint;
	String header;
	String payload;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCriteria() {
		return criteria;
	}

	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	public Subscription.SubscriptionChannelType getType() {
		return type;
	}

	public void setType(Subscription.SubscriptionChannelType type) {
		this.type = type;
	}

	public Subscription.SubscriptionStatus getStatus() {
		return status;
	}

	public void setStatus(Subscription.SubscriptionStatus status) {
		this.status = status;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
}
