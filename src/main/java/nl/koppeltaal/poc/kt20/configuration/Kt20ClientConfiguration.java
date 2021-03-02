package nl.koppeltaal.poc.kt20.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
@ConfigurationProperties(prefix = "kt20.client")
public class Kt20ClientConfiguration {
	String publicKey;
	boolean useJwe = true;

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public boolean isUseJwe() {
		return useJwe;
	}

	public void setUseJwe(boolean useJwe) {
		this.useJwe = useJwe;
	}


}
