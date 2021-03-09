/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.kt20.controllers;

import nl.koppeltaal.poc.kt20.KeyUtils;
import nl.koppeltaal.poc.kt20.configuration.Kt20ServerConfiguration;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 *
 */
@RestController
@RequestMapping(".well-known/jwks.json")
public class JwksController {
	private final Kt20ServerConfiguration kt20ServerConfiguration;

	public JwksController(Kt20ServerConfiguration kt20ServerConfiguration) {
		this.kt20ServerConfiguration = kt20ServerConfiguration;
	}

	@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public String get() throws InvalidKeySpecException, NoSuchAlgorithmException, JoseException {
		KeyPair rsaKeyPair = KeyUtils.getRsaKeyPair(kt20ServerConfiguration.getPublicKey(), kt20ServerConfiguration.getPrivateKey());
		JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(rsaKeyPair.getPublic());
		jsonWebKey.setKeyId(jsonWebKey.calculateBase64urlEncodedThumbprint("MD5"));
		JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jsonWebKey);
		return jsonWebKeySet.toJson();
	}

}
