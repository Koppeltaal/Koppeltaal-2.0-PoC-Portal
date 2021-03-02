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
