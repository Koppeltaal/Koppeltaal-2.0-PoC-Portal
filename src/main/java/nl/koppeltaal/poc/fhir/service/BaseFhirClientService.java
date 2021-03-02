/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import com.auth0.jwk.JwkException;
import nl.koppeltaal.poc.fhir.configuration.FhirClientConfiguration;
import nl.koppeltaal.poc.fhir.dto.BaseDto;
import nl.koppeltaal.poc.fhir.dto.DtoConverter;
import nl.koppeltaal.poc.generic.TokenStorage;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class BaseFhirClientService<D extends BaseDto, R extends DomainResource> {

	final FhirClientConfiguration fhirClientConfiguration;
	final Oauth2ClientService oauth2ClientService;
	final FhirContext fhirContext;
	final DtoConverter<D, R> dtoConverter;

	public BaseFhirClientService(FhirClientConfiguration fhirClientConfiguration, Oauth2ClientService oauth2ClientService, FhirContext fhirContext, DtoConverter<D, R> dtoConverter) {
		this.fhirClientConfiguration = fhirClientConfiguration;
		this.oauth2ClientService = oauth2ClientService;
		this.fhirContext = fhirContext;
		this.dtoConverter = dtoConverter;
	}

	public void deleteResource(TokenStorage sessionTokenStorage, String id) throws IOException, JwkException {
		IBaseOperationOutcome outcome = getFhirClient(sessionTokenStorage).delete().resourceById(getResourceName(), id).execute();
		System.out.println(outcome);
	}

	public void deleteResourceByReference(TokenStorage sessionTokenStorage, String id) throws IOException, JwkException {
		R resource = getResourceByReference(sessionTokenStorage, id);
		if (resource != null) {
			IBaseOperationOutcome outcome = getFhirClient(sessionTokenStorage).delete().resource(resource).execute();
			System.out.println(outcome);
		}
	}

	public R getResourceByReference(TokenStorage tokenStorage, String reference) throws IOException, JwkException {
		return (R) getFhirClient(tokenStorage).read().resource(getResourceName()).withId(reference).execute();
	}

	public R getResourceByIdentifier(TokenStorage tokenStorage, String identifierValue) throws IOException, JwkException {
		return getResourceByIdentifier(tokenStorage, identifierValue, getDefaultSystem());
	}

	public List<R> getResources(TokenStorage tokenStorage) throws JwkException, IOException {
		List<R> rv = new ArrayList<>();
		Bundle bundle = getFhirClient(tokenStorage).search().forResource(getResourceName()).returnBundle(Bundle.class).execute();
		for (Bundle.BundleEntryComponent component : bundle.getEntry()) {
			R resource = (R) component.getResource();
			rv.add(resource);
		}
		return rv;
	}

	public R storeResource(TokenStorage tokenStorage, String source, R resource) throws IOException, JwkException {
		String identifier = getIdentifier(getDefaultSystem(), resource);
		String id = getId(resource);
		R res = null;
		if (StringUtils.isNotEmpty(id)) {
			res = getResourceByReference(tokenStorage, id);
		} else if (StringUtils.isNotEmpty(identifier)) {
			res = getResourceByIdentifier(tokenStorage, identifier, getDefaultSystem());
		}


		if (res != null) {
			dtoConverter.applyDto(res, dtoConverter.convert(resource));
			getFhirClient(tokenStorage).update().resource(res).execute();
			return res;
		}

		updateMetaElement(source, resource);
		MethodOutcome execute = getFhirClient(tokenStorage).create().resource(resource).execute();
		return (R) execute.getResource();
	}

	protected abstract String getDefaultSystem();

	protected IGenericClient getFhirClient(TokenStorage tokenStorage) throws JwkException, IOException {

		IGenericClient iGenericClient = fhirContext.newRestfulGenericClient(fhirClientConfiguration.getBaseUrl());

		iGenericClient.registerInterceptor(new BearerTokenAuthInterceptor(oauth2ClientService.getAccessToken(tokenStorage)));

		return iGenericClient;


	}

	private String getId(R resource) {
		return resource.getIdElement().getIdPart();
	}

	protected abstract String getIdentifier(String system, R resource);

	protected R getResourceByIdentifier(TokenStorage tokenStorage, String identifierValue, String identifierSystem) throws JwkException, IOException {
		ICriterion<TokenClientParam> criterion = new TokenClientParam("identifier").exactly().systemAndIdentifier(identifierSystem, identifierValue);
		Bundle bundle = getFhirClient(tokenStorage).search().forResource(getResourceName()).where(criterion).returnBundle(Bundle.class).execute();
		if (bundle.getTotal() > 0) {
			Bundle.BundleEntryComponent bundleEntryComponent = bundle.getEntry().get(0);
			return (R) bundleEntryComponent.getResource();
		}
		return null;
	}

	protected abstract String getResourceName();

	private void updateMetaElement(String source, R resource) {
		Meta meta = resource.getMeta();
		if (meta == null) {
			meta = new Meta();
		}
		meta.setSource(source);
		resource.setMeta(meta);
	}
}
