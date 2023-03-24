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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.CareTeamDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.CareTeamDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.CareTeamFhirClientService;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

	@GetMapping
	public List<CareTeamDto> list(HttpSession httpSession) throws IOException, JwkException {

		Object user = httpSession.getAttribute("user");
		if (user instanceof Practitioner) {
			return super.list(httpSession);
		} else if (user instanceof Patient) {
			List<CareTeamDto> rv = new ArrayList<>();

			String reference = getReference((Patient) user);
			ICriterion<ReferenceClientParam> criterion = CareTeam.SUBJECT.hasId(reference);
			List<CareTeam> relatedPersons = fhirClientService.getResources( criterion);
			for (CareTeam careTeam : relatedPersons) {
				rv.add(dtoConverter.convert(careTeam));
			}
			return rv;
		} else {
			return Collections.emptyList();
		}
	}

	@PutMapping
	public CareTeamDto put(HttpSession httpSession, HttpServletRequest request, @RequestBody CareTeamDto dto) throws IOException {

		Object user = httpSession.getAttribute("user");

		if(!(user instanceof Patient)) {
			throw new SecurityException("Only Patients are allowed to create CareTeams");
		}

		dto.setStatus(CareTeam.CareTeamStatus.ACTIVE);

		final String userReference = getReference((Patient) user);
		final CareTeam convertedCareTeam = dtoConverter.convert(dto);
		convertedCareTeam.setSubject(new Reference(userReference));
		return dtoConverter.convert(fhirClientService.storeResource(convertedCareTeam));
	}
}
