/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.service;

import ca.uhn.fhir.context.FhirContext;
import nl.koppeltaal.poc.fhir.configuration.FhirClientConfiguration;
import nl.koppeltaal.poc.fhir.dto.PractitionerDto;
import nl.koppeltaal.poc.fhir.dto.PractitionerDtoConverter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class PractitionerFhirClientService extends BaseFhirClientService<PractitionerDto, Practitioner> {

	public PractitionerFhirClientService(FhirClientConfiguration fhirClientConfiguration, Oauth2ClientService oauth2ClientService, FhirContext fhirContext, PractitionerDtoConverter dtoConverter) {
		super(fhirClientConfiguration, oauth2ClientService, fhirContext, dtoConverter);
	}

	@Override
	protected String getDefaultSystem() {
		return "IRMA";
	}

	@Override
	protected String getResourceName() {
		return "Practitioner";
	}


}
