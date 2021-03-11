/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.dto;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class AuthorizationUrlDto {
	final Map<String, String> parameters = new HashMap<>();
	String url;
	String state;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void putParameter(String key, String value) {
		parameters.put(key, value);
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
