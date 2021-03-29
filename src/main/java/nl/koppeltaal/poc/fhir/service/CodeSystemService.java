/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Service
public class CodeSystemService {
	private static final Map<String, String> CODE_SYSTEMS = new HashMap<>();
	static {
		CODE_SYSTEMS.put("http://terminology.hl7.org/CodeSystem/v2-0131", "https://terminology.hl7.org/2.1.0/CodeSystem-v2-0131.json");
		CODE_SYSTEMS.put("http://terminology.hl7.org/CodeSystem/v3-RoleCode", "https://terminology.hl7.org/2.1.0/CodeSystem-v3-RoleCode.json");
	}

}
