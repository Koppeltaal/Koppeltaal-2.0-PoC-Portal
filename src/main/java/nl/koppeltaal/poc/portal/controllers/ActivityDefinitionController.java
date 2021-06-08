/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import com.auth0.jwk.JwkException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.ActivityDefinitionDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.ActivityDefinitionDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.ActivityDefinitionFhirClientService;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController()
@RequestMapping(value = "/api/ActivityDefinition")
public class ActivityDefinitionController extends BaseResourceController<ActivityDefinitionDto, ActivityDefinition> {

	final ActivityDefinitionFhirClientService fhirClientService;
	final ActivityDefinitionDtoConverter dtoConverter;

	public ActivityDefinitionController(ActivityDefinitionFhirClientService fhirClientService, ActivityDefinitionDtoConverter dtoConverter) {
		super(fhirClientService, dtoConverter);
		this.fhirClientService = fhirClientService;
		this.dtoConverter = dtoConverter;
	}

	@RequestMapping(value = "Patient/{patientId}", method = RequestMethod.GET)
	public List<ActivityDefinitionDto> getForPatients(@PathVariable String patientId) throws IOException, JwkException {
		List<ActivityDefinitionDto> rv = new ArrayList<>();
		List<ActivityDefinition> resources = fhirClientService.getResourcesForPatient("Patient/" + patientId);
		for (ActivityDefinition resource : resources) {
			rv.add(dtoConverter.convert(resource));
		}
		return rv;
	}

}
