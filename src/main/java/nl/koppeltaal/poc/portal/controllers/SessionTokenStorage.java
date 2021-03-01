/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import nl.koppeltaal.poc.generic.Oauth2TokenResponse;
import nl.koppeltaal.poc.generic.TokenStorage;

import javax.servlet.http.HttpSession;

/**
 *
 */
public class SessionTokenStorage implements TokenStorage {
	final HttpSession httpSession;

	public SessionTokenStorage(HttpSession httpSession) {
		this.httpSession = httpSession;
	}

	@Override
	public void clear() {
		httpSession.removeAttribute("credentials");
	}

	@Override
	public Oauth2TokenResponse getToken() {
		return (Oauth2TokenResponse) httpSession.getAttribute("credentials");
	}

	public boolean hasToken() {
		return httpSession.getAttribute("credentials") != null;
	}

	@Override
	public void updateToken(Oauth2TokenResponse token) {
		httpSession.setAttribute("credentials", token);
	}
}
