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
import nl.koppeltaal.poc.fhir.dto.SubscriptionDto;
import nl.koppeltaal.poc.fhir.dto.SubscriptionDtoConverter;
import org.hl7.fhir.r4.model.Subscription;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class SubscriptionFhirClientService extends BaseFhirClientService<SubscriptionDto, Subscription> {

	public SubscriptionFhirClientService(FhirClientConfiguration fhirClientConfiguration, Oauth2ClientService oauth2ClientService, FhirContext fhirContext, SubscriptionDtoConverter subscriptionDtoConverter) {
		super(fhirClientConfiguration, oauth2ClientService, fhirContext, subscriptionDtoConverter);
	}

	@Override
	protected String getResourceName() {
		return "Subscription";
	}
	protected String getDefaultSystem() {
		return "subscription-no-identifier";
	}


}
