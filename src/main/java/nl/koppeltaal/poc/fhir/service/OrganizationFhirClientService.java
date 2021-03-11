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
import nl.koppeltaal.poc.fhir.dto.OrganizationDto;
import nl.koppeltaal.poc.fhir.dto.OrganizationDtoConverter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class OrganizationFhirClientService extends BaseFhirClientService<OrganizationDto, Organization> {

	public OrganizationFhirClientService(FhirClientConfiguration fhirClientConfiguration, Oauth2ClientService oauth2ClientService, FhirContext fhirContext, OrganizationDtoConverter organizationDtoConverter) {
		super(fhirClientConfiguration, oauth2ClientService, fhirContext, organizationDtoConverter);
	}

	@Override
	protected String getResourceName() {
		return "Organization";
	}

	protected String getDefaultSystem() {
		return "http://fhir.nl/fhir/NamingSystem/agb-z";
	}

}
