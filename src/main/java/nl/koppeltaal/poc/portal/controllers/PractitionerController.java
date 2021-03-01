/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import com.auth0.jwk.JwkException;
import nl.koppeltaal.poc.fhir.dto.PractitionerDto;
import nl.koppeltaal.poc.fhir.dto.PractitionerDtoConverter;
import nl.koppeltaal.poc.fhir.exception.EnitityNotFoundException;
import nl.koppeltaal.poc.fhir.service.PractitionerFhirClientService;
import nl.koppeltaal.poc.utils.UrlUtils;
import org.hl7.fhir.r4.model.Practitioner;
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
@RequestMapping("/api/practitioners")
public class PractitionerController {


	final PractitionerFhirClientService fhirClientService;
	final PractitionerDtoConverter dtoConverter;

	public PractitionerController(PractitionerFhirClientService fhirClientService, PractitionerDtoConverter dtoConverter) {
		this.fhirClientService = fhirClientService;
		this.dtoConverter = dtoConverter;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<PractitionerDto> list(HttpSession httpSession) throws IOException, JwkException {
		List<PractitionerDto> rv = new ArrayList<>();
		List<Practitioner> practitioners = fhirClientService.getResources(new SessionTokenStorage(httpSession));
		for (Practitioner practitioner : practitioners) {
			rv.add(dtoConverter.convert(practitioner));
		}
		return rv;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public PractitionerDto put(HttpSession httpSession, HttpServletRequest request, @RequestBody PractitionerDto practitionersDto) throws IOException, JwkException {
		return dtoConverter.convert(fhirClientService.storeResource(new SessionTokenStorage(httpSession), UrlUtils.getServerUrl("", request), dtoConverter.convert(practitionersDto)));
	}

	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	public void delete(HttpSession httpSession, @PathVariable String id) throws IOException, JwkException {
		fhirClientService.deleteResource(new SessionTokenStorage(httpSession), id);
	}

	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public PractitionerDto get(HttpSession httpSession, @PathVariable String id) throws IOException, JwkException {
		Practitioner practitioner = fhirClientService.getResourceById(new SessionTokenStorage(httpSession), id);
		if (practitioner != null) {
			return dtoConverter.convert(practitioner);
		} else {
			throw new EnitityNotFoundException("Cannot locate practitioner " + id);
		}
	}
}
