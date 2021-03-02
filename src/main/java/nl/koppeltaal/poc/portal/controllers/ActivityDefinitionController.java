/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import com.auth0.jwk.JwkException;
import nl.koppeltaal.poc.fhir.dto.ActivityDefinitionDto;
import nl.koppeltaal.poc.fhir.dto.ActivityDefinitionDtoConverter;
import nl.koppeltaal.poc.fhir.exception.EnitityNotFoundException;
import nl.koppeltaal.poc.fhir.service.ActivityDefinitionFhirClientService;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;

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

}
