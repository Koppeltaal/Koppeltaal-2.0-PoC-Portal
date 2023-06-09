/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

/**
 *
 */
@Controller
public class IndexController {

	@RequestMapping(value = {"/"})
	public String index(HttpSession httpSession, Authentication authentication) {

		if (httpSession.getAttribute("user") instanceof Patient) {
			return "redirect:patient/index.html";
		} else if (httpSession.getAttribute("user") instanceof Practitioner) {
			return "redirect:practitioner/index.html";
		}
		if (!authentication.isAuthenticated()) {
			return "redirect:/sso/login";
		}
		return "unknown.html";
	}
}
