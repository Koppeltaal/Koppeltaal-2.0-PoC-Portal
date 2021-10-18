/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import nl.koppeltaal.spring.boot.starter.smartservice.dto.CareTeamDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.CareTeamDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.CareTeamFhirClientService;
import org.hl7.fhir.r4.model.CareTeam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController()
@RequestMapping("/api/CareTeam")
public class CareTeamController extends BaseResourceController<CareTeamDto, CareTeam> {

	public CareTeamController(CareTeamFhirClientService fhirClientService, CareTeamDtoConverter dtoConverter) {
		super(fhirClientService, dtoConverter);
	}
}
