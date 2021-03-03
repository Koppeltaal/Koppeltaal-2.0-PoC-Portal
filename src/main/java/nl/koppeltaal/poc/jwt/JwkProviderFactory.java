package nl.koppeltaal.poc.jwt;

import com.auth0.jwk.GuavaCachedJwkProvider;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class JwkProviderFactory {
	final private static Map<String, JwkProvider> CACHE = new HashMap<>();

	public static JwkProvider getJwkProvider(String issuer) {
		if (CACHE.containsKey(issuer)) {
			return CACHE.get(issuer);
		}
		JwkProvider provider = new GuavaCachedJwkProvider(new UrlJwkProvider(issuer));
		CACHE.put(issuer, provider);
		return provider;
	}
}
