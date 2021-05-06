/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IFetchConformanceTyped;
import ca.uhn.fhir.rest.gclient.IFetchConformanceUntyped;
import nl.koppeltaal.poc.fhir.configuration.FhirClientConfiguration;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Extension;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class FhirCapabilitiesService {
	final FhirContext fhirContext;
	final FhirClientConfiguration fhirClientConfiguration;

	public FhirCapabilitiesService(FhirContext fhirContext, FhirClientConfiguration fhirClientConfiguration) {
		this.fhirContext = fhirContext;
		this.fhirClientConfiguration = fhirClientConfiguration;
	}

	public String getTokenUrl() {
		IGenericClient client = fhirContext.newRestfulGenericClient(fhirClientConfiguration.getServerUrl());
		IFetchConformanceUntyped capabilities = client.capabilities();
		IFetchConformanceTyped<CapabilityStatement> conformanceTyped = capabilities.ofType(CapabilityStatement.class);
		CapabilityStatement capabilityStatement = conformanceTyped.execute();
		List<CapabilityStatement.CapabilityStatementRestComponent> rest = capabilityStatement.getRest();
		for (CapabilityStatement.CapabilityStatementRestComponent capabilityStatementRestComponent : rest) {
			List<Extension> extensionsByUrl = capabilityStatementRestComponent.getSecurity().getExtensionsByUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");
			for (Extension extension : extensionsByUrl) {
				Extension token = extension.getExtensionByUrl("token");
				if (token != null) {
					return token.getValue().primitiveValue();
				}

			}
		}
		return null;
	}
}
