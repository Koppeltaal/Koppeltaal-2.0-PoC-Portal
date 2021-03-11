/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import nl.koppeltaal.poc.fhir.dto.PatientDtoConverter;
import nl.koppeltaal.poc.fhir.service.PatientFhirClientService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController()
@RequestMapping("/api/Patient")
public class PatientsController extends BaseResourceController {


	final PatientFhirClientService fhirClientService;
	final PatientDtoConverter dtoConverter;

	public PatientsController(PatientFhirClientService fhirClientService, PatientDtoConverter dtoConverter) {
		super(fhirClientService, dtoConverter);
		this.fhirClientService = fhirClientService;
		this.dtoConverter = dtoConverter;
	}


}
