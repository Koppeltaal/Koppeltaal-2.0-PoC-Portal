/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import nl.koppeltaal.spring.boot.starter.smartservice.dto.PatientDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.PatientDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PatientFhirClientService;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController()
@RequestMapping("/api/Patient")
public class PatientsController extends BaseResourceController<PatientDto, Patient> {

	public PatientsController(PatientFhirClientService fhirClientService, PatientDtoConverter dtoConverter) {
		super(fhirClientService, dtoConverter);
	}


}
