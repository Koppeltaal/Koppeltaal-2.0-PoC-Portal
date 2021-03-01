/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import com.auth0.jwk.JwkException;
import com.auth0.jwt.interfaces.DecodedJWT;
import nl.koppeltaal.poc.fhir.dto.AuthorizationUrlDto;
import nl.koppeltaal.poc.fhir.service.Oauth2ClientService;
import nl.koppeltaal.poc.fhir.service.PatientFhirClientService;
import nl.koppeltaal.poc.fhir.service.PractitionerFhirClientService;
import nl.koppeltaal.poc.jwt.JwtValidationService;
import nl.koppeltaal.poc.utils.UrlUtils;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Assert;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 *
 */
@Controller

public class LoginController {

	final Oauth2ClientService oauth2ClientService;
	final PatientFhirClientService patientFhirClientService;
	final PractitionerFhirClientService practitionerFhirClientService;

	public LoginController(Oauth2ClientService oauth2ClientService, PatientFhirClientService patientFhirClientService, PractitionerFhirClientService practitionerFhirClientService) {
		this.oauth2ClientService = oauth2ClientService;
		this.patientFhirClientService = patientFhirClientService;
		this.practitionerFhirClientService = practitionerFhirClientService;
	}

	@RequestMapping("code_response")
	public String codeResponse(HttpSession httpSession, HttpServletRequest request, String code, String state) throws IOException, JwkException {
		Assert.assertEquals(state, httpSession.getAttribute("state"));
		SessionTokenStorage tokenStorage = new SessionTokenStorage(httpSession);



		oauth2ClientService.getToken(code, UrlUtils.getServerUrl("/code_response", request), tokenStorage);

		String userIdFromCredentials = oauth2ClientService.getUserIdFromCredentials(tokenStorage);
		Patient patient = patientFhirClientService.getResourceByIdentifier(tokenStorage, userIdFromCredentials);
		Practitioner practitioner = practitionerFhirClientService.getResourceByIdentifier(tokenStorage, userIdFromCredentials);
		if  (practitioner != null) {
			httpSession.setAttribute("user", practitioner);
			return "redirect:practitioner/index.html";
		} else if (patient != null) {
			httpSession.setAttribute("user", patient);
			return "redirect:patient/index.html";
		}


		return "redirect:unknown.html";

	}

	@RequestMapping("/login")
	public View login(HttpSession httpSession, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		AuthorizationUrlDto authorizationUrl = oauth2ClientService.getAuthorizationUrl(UrlUtils.getServerUrl("", request), UrlUtils.getServerUrl("/code_response", request));
		httpSession.setAttribute("state", authorizationUrl.getState());
		redirectAttributes.addAllAttributes(authorizationUrl.getParameters());
		return new RedirectView(authorizationUrl.getUrl());
	}

	@RequestMapping("/logout")
	public String logout(HttpSession httpSession, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		httpSession.removeAttribute("credentials");
		return "redirect:index.html";
	}

}
