package nl.koppeltaal.poc.generic;

/**
 *
 */
public interface TokenStorage {
	void clear();

	IdTokenResponse getIdToken();

	void updateToken(IdTokenResponse token);
}
