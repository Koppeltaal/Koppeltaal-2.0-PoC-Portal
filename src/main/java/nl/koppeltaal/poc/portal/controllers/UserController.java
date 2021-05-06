/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import com.auth0.jwk.JwkException;
import nl.koppeltaal.poc.fhir.dto.*;
import nl.koppeltaal.poc.fhir.service.PatientFhirClientService;
import nl.koppeltaal.poc.oidc.service.OidcClientService;
import nl.koppeltaal.poc.portal.dto.UserDto;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 *
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

	final OidcClientService oidcClientService;
	final PatientDtoConverter patientDtoConverter;
	final PractitionerDtoConverter practitionerDtoConverter;
	final RelatedPersonDtoConverter relatedPersonDtoConverter;
	final PatientFhirClientService patientFhirClientService;

	public UserController(OidcClientService oidcClientService, PatientDtoConverter patientDtoConverter, PractitionerDtoConverter practitionerDtoConverter, RelatedPersonDtoConverter relatedPersonDtoConverter, PatientFhirClientService patientFhirClientService) {
		this.oidcClientService = oidcClientService;
		this.patientDtoConverter = patientDtoConverter;
		this.practitionerDtoConverter = practitionerDtoConverter;
		this.relatedPersonDtoConverter = relatedPersonDtoConverter;
		this.patientFhirClientService = patientFhirClientService;
	}

	@RequestMapping(value = "current", method = RequestMethod.GET)
	public UserDto getUser(HttpSession httpSession) throws JwkException, IOException {
		UserDto rv = new UserDto();
		SessionTokenStorage tokenStorage = new SessionTokenStorage(httpSession);
		if (tokenStorage.hasIdToken()) {
			rv.setUserId(oidcClientService.getUserIdFromCredentials(tokenStorage));
			rv.setUserIdentifier(oidcClientService.getUserIdentifierFromCredentials(tokenStorage));
			rv.setLoggedIn(true);
		} else {
			rv.setLoggedIn(false);
		}

		if (rv.getLoggedIn()) {
			Object user = httpSession.getAttribute("user");
			if (user instanceof Patient) {
				PatientDto dto = patientDtoConverter.convert((Patient) user);
				rv.setPatient(dto);
				rv.setType("Patient");
				rv.setNameGiven(dto.getNameGiven());
				rv.setNameFamily(dto.getNameFamily());
			} else if (user instanceof Practitioner) {
				PractitionerDto dto = practitionerDtoConverter.convert((Practitioner) user);
				rv.setNameGiven(dto.getNameGiven());
				rv.setType("Practitioner");
				rv.setNameFamily(dto.getNameFamily());
			} else if (user instanceof RelatedPerson) {
				RelatedPersonDto dto = relatedPersonDtoConverter.convert((RelatedPerson) user);
				String patient = dto.getPatient();
				rv.setType("RelatedPerson");
				rv.setPatient(patientDtoConverter.convert(patientFhirClientService.getResourceByReference(patient)));
				rv.setNameGiven(dto.getNameGiven());
				rv.setNameFamily(dto.getNameFamily());
			}
		}
		return rv;
	}
}
