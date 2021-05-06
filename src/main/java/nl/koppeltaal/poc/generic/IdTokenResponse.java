package nl.koppeltaal.poc.generic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 *
 */
public class IdTokenResponse implements Serializable {
	@JsonProperty("id_token")
	String idToken;
	@JsonProperty("token_type")
	String tokenType;

	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

}
