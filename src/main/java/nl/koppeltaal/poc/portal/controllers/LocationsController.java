/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import nl.koppeltaal.spring.boot.starter.smartservice.dto.LocationDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.LocationDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.LocationFhirClientService;
import org.hl7.fhir.r4.model.Location;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController()
@RequestMapping("/api/Location")
public class LocationsController extends BaseResourceController<LocationDto, Location> {

	public LocationsController(LocationFhirClientService fhirClientService, LocationDtoConverter dtoConverter) {
		super(fhirClientService, dtoConverter);
	}

}
