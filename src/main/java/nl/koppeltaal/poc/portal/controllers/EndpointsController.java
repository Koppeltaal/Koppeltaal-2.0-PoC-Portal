/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import nl.koppeltaal.poc.fhir.dto.EndpointDto;
import nl.koppeltaal.poc.fhir.dto.EndpointDtoConverter;
import nl.koppeltaal.poc.fhir.service.EndpointFhirClientService;
import org.hl7.fhir.r4.model.Endpoint;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController()
@RequestMapping("/api/Endpoint")
public class EndpointsController extends BaseResourceController<EndpointDto, Endpoint> {

	public EndpointsController(EndpointFhirClientService fhirClientService, EndpointDtoConverter dtoConverter) {
		super(fhirClientService, dtoConverter);
	}

}
