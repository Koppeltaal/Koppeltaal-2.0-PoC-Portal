package nl.koppeltaal.poc.kt20.valueobjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {
	String resourceType = "Task";
	String id;
	Meta meta;
	Text text;
	Reference definitionReference = new Reference();
	List<Identifier> identifier = new ArrayList();
	String status = "requested";
	String intent = "plan";
	@JsonProperty("for")
	User forUser;
	User requester;
	User owner;

	public Reference getDefinitionReference() {
		return definitionReference;
	}

	public User getForUser() {
		return forUser;
	}

	public void setForUser(User forUser) {
		this.forUser = forUser;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Identifier> getIdentifier() {
		return identifier;
	}

	public void setIdentifier(List<Identifier> identifier) {
		this.identifier = identifier;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public User getRequester() {
		return requester;
	}

	public void setRequester(User requester) {
		this.requester = requester;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class Meta {
		String versionId;
		Date lastUpdated;
		String source = "https://issuer.edia.nl/fhir";

		public Date getLastUpdated() {
			return lastUpdated;
		}

		public void setLastUpdated(Date lastUpdated) {
			this.lastUpdated = lastUpdated;
		}

		public String getSource() {
			return source;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public String getVersionId() {
			return versionId;
		}

		public void setVersionId(String versionId) {
			this.versionId = versionId;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static final class Text {
		String status;
		String div;

		public String getDiv() {
			return div;
		}

		public void setDiv(String div) {
			this.div = div;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Identifier {
		String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Reference {
		String reference;

		public String getReference() {
			return reference;
		}

		public void setReference(String reference) {
			this.reference = reference;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class User extends Reference {
		//		UserType type;
		String display;

		public String getDisplay() {
			return display;
		}

		public void setDisplay(String display) {
			this.display = display;
		}

//		public UserType getType() {
//			return type;
//		}
//
//		public void setType(UserType type) {
//			this.type = type;
//		}

		public enum UserType {
			Patient,
			Practitioner,
			RelatedPerson
		}
	}
}
