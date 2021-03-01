package nl.koppeltaal.poc.generic;

/**
 *
 */
public interface TokenStorage {
	void clear();

	Oauth2TokenResponse getToken();

	void updateToken(Oauth2TokenResponse token);
}
