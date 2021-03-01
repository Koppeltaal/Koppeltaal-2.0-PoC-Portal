/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import com.auth0.jwk.JwkException;
import nl.koppeltaal.poc.fhir.dto.PatientDto;
import nl.koppeltaal.poc.fhir.dto.PatientDtoConverter;
import nl.koppeltaal.poc.fhir.exception.EnitityNotFoundException;
import nl.koppeltaal.poc.fhir.service.PatientFhirClientService;
import nl.koppeltaal.poc.utils.UrlUtils;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@RestController()
@RequestMapping("/api/patients")
public class PatientsController {


	final PatientFhirClientService fhirClientService;
	final PatientDtoConverter dtoConverter;

	public PatientsController(PatientFhirClientService fhirClientService, PatientDtoConverter dtoConverter) {
		this.fhirClientService = fhirClientService;
		this.dtoConverter = dtoConverter;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<PatientDto> list(HttpSession httpSession) throws IOException, JwkException {
		List<PatientDto> rv = new ArrayList<>();
		List<Patient> patients = fhirClientService.getResources(new SessionTokenStorage(httpSession));
		for (Patient patient : patients) {
			rv.add(dtoConverter.convert(patient));
		}
		return rv;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public PatientDto put(HttpSession httpSession, HttpServletRequest request, @RequestBody PatientDto patientDto) throws IOException, JwkException {
		return dtoConverter.convert(fhirClientService.storeResource(new SessionTokenStorage(httpSession), UrlUtils.getServerUrl("", request), dtoConverter.convert(patientDto)));
	}

	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	public void delete(HttpSession httpSession, @PathVariable String id) throws IOException, JwkException {
		fhirClientService.deleteResource(new SessionTokenStorage(httpSession), id);
	}

	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public PatientDto get(HttpSession httpSession, @PathVariable String id) throws IOException, JwkException {
		Patient patient = fhirClientService.getResourceById(new SessionTokenStorage(httpSession), id);
		if (patient != null) {
			return dtoConverter.convert(patient);
		} else {
			throw new EnitityNotFoundException("Cannot locate patient " + id);
		}
	}
}
