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

	public OAuth2Urls getOAuth2Urls() {
		OAuth2Urls oAuth2Urls = new OAuth2Urls();
		IGenericClient client = fhirContext.newRestfulGenericClient(fhirClientConfiguration.getBaseUrl());
		IFetchConformanceUntyped capabilities = client.capabilities();
		IFetchConformanceTyped<CapabilityStatement> conformanceTyped = capabilities.ofType(CapabilityStatement.class);
		CapabilityStatement capabilityStatement = conformanceTyped.execute();
		List<CapabilityStatement.CapabilityStatementRestComponent> rest = capabilityStatement.getRest();
		for (CapabilityStatement.CapabilityStatementRestComponent capabilityStatementRestComponent : rest) {
			List<Extension> extensionsByUrl = capabilityStatementRestComponent.getSecurity().getExtensionsByUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");
			for (Extension extension : extensionsByUrl) {
				Extension token = extension.getExtensionByUrl("token");
				if (token != null) {
					oAuth2Urls.setTokenUrl(token.getValue().primitiveValue());
				}
				Extension authorize = extension.getExtensionByUrl("authorize");
				if (authorize != null) {
					oAuth2Urls.setAuthorizeUrl(authorize.getValue().primitiveValue());
				}

			}
		}
		return oAuth2Urls;
	}

	public static class OAuth2Urls {
		String tokenUrl;
		String authorizeUrl;

		public String getAuthorizeUrl() {
			return authorizeUrl;
		}

		public void setAuthorizeUrl(String authorizeUrl) {
			this.authorizeUrl = authorizeUrl;
		}

		public String getTokenUrl() {
			return tokenUrl;
		}

		public void setTokenUrl(String tokenUrl) {
			this.tokenUrl = tokenUrl;
		}
	}
}
