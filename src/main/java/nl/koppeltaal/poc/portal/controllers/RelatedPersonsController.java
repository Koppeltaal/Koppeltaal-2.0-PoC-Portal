/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import com.auth0.jwk.JwkException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpSession;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.PatientDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.PatientDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.RelatedPersonDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.RelatedPersonDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.RelatedPersonFhirClientService;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController()
@RequestMapping("/api/RelatedPerson")
public class RelatedPersonsController extends BaseResourceController<RelatedPersonDto, RelatedPerson> {

	final RelatedPersonFhirClientService fhirClientService;
	final PatientDtoConverter patientDtoConverter;
	public RelatedPersonsController(RelatedPersonFhirClientService fhirClientService, RelatedPersonDtoConverter dtoConverter, PatientDtoConverter patientDtoConverter) {
		super(fhirClientService, dtoConverter);
		this.fhirClientService = fhirClientService;
		this.patientDtoConverter = patientDtoConverter;
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<RelatedPersonDto> list(HttpSession httpSession) throws IOException, JwkException {
		Object user = httpSession.getAttribute("user");
		if (user instanceof Practitioner) {
			return super.list(httpSession);
		} else if (user instanceof Patient) {
			List<RelatedPersonDto> rv = new ArrayList<>();

			Patient patient = (Patient) user;
			PatientDto patientDto = patientDtoConverter.convert(patient);

			ICriterion<ReferenceClientParam> criterion = RelatedPerson.PATIENT.hasId(patientDto.getReference());
			List<RelatedPerson> relatedPersons = fhirClientService.getResources( criterion);
			for (RelatedPerson relatedPerson : relatedPersons) {
				rv.add(dtoConverter.convert(relatedPerson));
			}
			return rv;
		} else {
			return Collections.emptyList();
		}
	}


}
