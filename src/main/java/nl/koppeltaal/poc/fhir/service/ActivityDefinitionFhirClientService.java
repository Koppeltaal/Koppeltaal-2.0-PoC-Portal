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
import nl.koppeltaal.poc.fhir.dto.ActivityDefinitionDto;
import nl.koppeltaal.poc.fhir.dto.ActivityDefinitionDtoConverter;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Identifier;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class ActivityDefinitionFhirClientService extends BaseFhirClientService<ActivityDefinitionDto, ActivityDefinition> {

	public ActivityDefinitionFhirClientService(FhirClientConfiguration fhirClientConfiguration, Oauth2ClientService oauth2ClientService, FhirContext fhirContext, ActivityDefinitionDtoConverter activityDefinitionDtoConverter) {
		super(fhirClientConfiguration, oauth2ClientService, fhirContext, activityDefinitionDtoConverter);
	}

	@Override
	protected String getDefaultSystem() {
		return "http:/vzvz.nl/artifacts";
	}

	protected String getIdentifier(String system, ActivityDefinition resource) {
		for (Identifier identifier : resource.getIdentifier()) {
			if (StringUtils.equals(identifier.getSystem(), system)) {
				return identifier.getValue();
			}
		}
		return null;
	}

	@Override
	protected String getResourceName() {
		return "ActivityDefinition";
	}


}
