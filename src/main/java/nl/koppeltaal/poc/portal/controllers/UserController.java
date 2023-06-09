/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import nl.koppeltaal.poc.portal.dto.UserDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.PatientDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.PatientDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.PractitionerDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.PractitionerDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.RelatedPersonDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.RelatedPersonDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PatientFhirClientService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PractitionerFhirClientService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.RelatedPersonFhirClientService;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 *
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

	final PatientDtoConverter patientDtoConverter;
	final PractitionerDtoConverter practitionerDtoConverter;
	final RelatedPersonDtoConverter relatedPersonDtoConverter;
	final PatientFhirClientService patientFhirClientService;


	public UserController(PatientDtoConverter patientDtoConverter, PractitionerDtoConverter practitionerDtoConverter, RelatedPersonDtoConverter relatedPersonDtoConverter, PatientFhirClientService patientFhirClientService, PractitionerFhirClientService practitionerFhirClientService, RelatedPersonFhirClientService relatedPersonFhirClientService) {
		this.patientDtoConverter = patientDtoConverter;
		this.practitionerDtoConverter = practitionerDtoConverter;
		this.relatedPersonDtoConverter = relatedPersonDtoConverter;
		this.patientFhirClientService = patientFhirClientService;
	}

	@RequestMapping(value = "current", method = RequestMethod.GET)
	public UserDto getUser(Authentication authentication, HttpSession httpSession) {
		OidcUser oidcUser = (OidcUser) authentication;
		UserDto rv = new UserDto();
		String principalName = oidcUser.getName();
		if (StringUtils.isNotBlank(principalName)) {
			rv.setUserId(principalName);
			rv.setUserIdentifier(principalName);
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
