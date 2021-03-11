/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.service;

import com.auth0.jwk.JwkException;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.koppeltaal.poc.fhir.configuration.FhirClientConfiguration;
import nl.koppeltaal.poc.fhir.dto.AuthorizationUrlDto;
import nl.koppeltaal.poc.generic.Oauth2TokenResponse;
import nl.koppeltaal.poc.generic.TokenStorage;
import nl.koppeltaal.poc.jwt.JwtValidationService;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 */
@Service
public class Oauth2ClientService {

	final FhirCapabilitiesService fhirCapabilitiesService;
	final JwtValidationService jwtValidationService;
	final FhirClientConfiguration fhirClientConfiguration;

	public Oauth2ClientService(FhirCapabilitiesService fhirCapabilitiesService, JwtValidationService jwtValidationService, FhirClientConfiguration fhirClientConfiguration) {
		this.fhirCapabilitiesService = fhirCapabilitiesService;
		this.jwtValidationService = jwtValidationService;
		this.fhirClientConfiguration = fhirClientConfiguration;
	}

	public void checkCredentials(TokenStorage tokenStorage) throws JwkException, IOException {
		try {
			Oauth2TokenResponse token = tokenStorage.getToken();
			if (token != null) {
				jwtValidationService.validate(token.getIdToken(), fhirClientConfiguration.getClientId());
				jwtValidationService.validate(token.getAccessToken(), null);
			}
		} catch (TokenExpiredException e) {
			refreshToken(tokenStorage);
		}
	}

	public String getAccessToken(TokenStorage tokenStorage) throws JwkException, IOException {
		if (tokenStorage.getToken() != null) {
			checkCredentials(tokenStorage);
			return tokenStorage.getToken().getAccessToken();
		}
		return "";
	}

	public AuthorizationUrlDto getAuthorizationUrl(String serverUrl, String redirectUrl) {
		AuthorizationUrlDto rv = new AuthorizationUrlDto();
		String authorizeUrl = fhirCapabilitiesService.getOAuth2Urls().getAuthorizeUrl();
		rv.putParameter("response_type", "code");
		rv.putParameter("client_id", fhirClientConfiguration.getClientId());
		rv.putParameter("redirect_uri", redirectUrl);
		rv.putParameter("scope", "openid user/Patient.* user/Practitioner.*");
		String state = UUID.randomUUID().toString();
		rv.putParameter("state", state);
		rv.putParameter("aud", serverUrl);

		rv.setUrl(authorizeUrl);
		rv.setState(state);
		return rv;
	}

	public void getToken(String code, String redirectUri, TokenStorage tokenStorage) throws IOException {
		String tokenUrl = fhirCapabilitiesService.getOAuth2Urls().getTokenUrl();
		try (CloseableHttpClient httpClient = createHttpClient()) {

			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("grant_type", "authorization_code"));
			params.add(new BasicNameValuePair("redirect_uri", redirectUri));
			params.add(new BasicNameValuePair("code", code));

			// TODO: add security

			final HttpPost httpPost = new HttpPost(tokenUrl);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			CloseableHttpResponse response = httpClient.execute(httpPost);
			try (InputStream in = response.getEntity().getContent()) {
				ObjectMapper objectMapper = new ObjectMapper();
				tokenStorage.updateToken(objectMapper.readValue(in, Oauth2TokenResponse.class));
			}
		}
	}

	public String getUserIdFromCredentials(TokenStorage tokenStorage) throws JwkException, IOException {
		try {
			return jwtValidationService.validate(tokenStorage.getToken().getIdToken(), fhirClientConfiguration.getClientId(), 60).getSubject();
		} catch (TokenExpiredException | InvalidClaimException  e) { // The implementation JWTVerifier.assertDateIsPast(JWTVerifier.java:479) throws an InvalidClaimException.
			refreshToken(tokenStorage);
			return jwtValidationService.validate(tokenStorage.getToken().getIdToken(), fhirClientConfiguration.getClientId(), 60).getSubject();
		}

	}

	public void refreshToken(TokenStorage tokenStorage) throws IOException {
		String tokenUrl = fhirCapabilitiesService.getOAuth2Urls().getTokenUrl();
		try (CloseableHttpClient httpClient = createHttpClient()) {

			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("grant_type", "refresh_token"));
			params.add(new BasicNameValuePair("refresh_token", tokenStorage.getToken().getRefreshToken()));
			params.add(new BasicNameValuePair("scope", "openid user/Patient.* user/Practitioner.*"));

			// TODO: add security

			final HttpPost httpPost = new HttpPost(tokenUrl);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			CloseableHttpResponse response = httpClient.execute(httpPost);
			try (InputStream in = response.getEntity().getContent()) {
				String content = IOUtils.toString(new InputStreamReader(in, Charset.defaultCharset()));
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					tokenStorage.updateToken(objectMapper.readValue(content, Oauth2TokenResponse.class));
				} catch (JsonParseException e) {
					System.out.println(content);
					throw e;
				}
			}
		}

	}

	private CloseableHttpClient createHttpClient() {
		return HttpClients.createDefault();
	}

}
