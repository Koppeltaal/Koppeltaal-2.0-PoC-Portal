/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import nl.koppeltaal.spring.boot.starter.smartservice.dto.PractitionerDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.PractitionerDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PractitionerFhirClientService;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController()
@RequestMapping("/api/Practitioner")
public class PractitionerController extends BaseResourceController<PractitionerDto, Practitioner> {

	public PractitionerController(PractitionerFhirClientService fhirClientService, PractitionerDtoConverter dtoConverter) {
		super(fhirClientService, dtoConverter);
	}

}
