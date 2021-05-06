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
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@SuppressWarnings("unchecked")
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

	public void deleteResource(String id) throws IOException, JwkException {
		getFhirClient().delete().resourceById(getResourceName(), id).execute();
	}

	public void deleteResourceByReference(String id) throws IOException, JwkException {
		R resource = getResourceByReference(id);
		if (resource != null) {
			getFhirClient().delete().resource(resource).execute();
		}
	}

	public R getResourceByIdentifier(Identifier identifier) throws IOException, JwkException {
		String system = StringUtils.isNotEmpty(identifier.getSystem()) ? identifier.getSystem() : getDefaultSystem();
		return getResourceByIdentifier(system, identifier.getValue());
	}

	public R getResourceByIdentifier(String identifierValue) throws IOException, JwkException {
		return getResourceByIdentifier(identifierValue, getDefaultSystem());
	}

	public R getResourceByReference(String reference) throws IOException, JwkException {
		return (R) getFhirClient().read().resource(getResourceName()).withId(reference).execute();
	}

	public R getResourceByReference(Reference reference) throws IOException, JwkException {
		String ref = reference.getReference();
		if (StringUtils.isNotEmpty(ref)) {
			return getResourceByReference(ref);
		} else if (reference.getIdentifier() != null) {
			return getResourceByIdentifier(reference.getIdentifier());
		}
		return null;
	}

	public List<R> getResources() throws JwkException, IOException {
		List<R> rv = new ArrayList<>();
		Bundle bundle = getFhirClient().search().forResource(getResourceName()).returnBundle(Bundle.class).execute();
		for (Bundle.BundleEntryComponent component : bundle.getEntry()) {
			R resource = (R) component.getResource();
			rv.add(resource);
		}
		return rv;
	}

	public List<R> getResources(ICriterion<?> criterion) throws JwkException, IOException {
		List<R> rv = new ArrayList<>();
		Bundle bundle = getFhirClient().search().forResource(getResourceName()).where(criterion).returnBundle(Bundle.class).execute();
		for (Bundle.BundleEntryComponent component : bundle.getEntry()) {
			R resource = (R) component.getResource();
			rv.add(resource);
		}
		return rv;
	}

	public R storeResource(String source, R resource) throws IOException, JwkException {
		String identifier = getIdentifier(getDefaultSystem(), resource);
		String id = getId(resource);
		R res = null;
		if (StringUtils.isNotEmpty(id)) {
			res = getResourceByReference(id);
		} else if (StringUtils.isNotEmpty(identifier)) {
			res = getResourceByIdentifier(identifier, getDefaultSystem());
		}


		if (res != null) {
			dtoConverter.applyDto(res, dtoConverter.convert(resource));
			MethodOutcome execute = getFhirClient().update().resource(res).execute();
			return (R) execute.getResource();
		}

		updateMetaElement(source, resource);
		MethodOutcome execute = getFhirClient().create().resource(resource).execute();
		return (R) execute.getResource();
	}

	protected abstract String getDefaultSystem();

	protected IGenericClient getFhirClient() throws JwkException, IOException {

		IGenericClient iGenericClient = fhirContext.newRestfulGenericClient(fhirClientConfiguration.getServerUrl());

		iGenericClient.registerInterceptor(new BearerTokenAuthInterceptor(oauth2ClientService.getAccessToken()));

		return iGenericClient;


	}

	private String getId(R resource) {
		IdType idElement = resource.getIdElement();
		if (!idElement.isEmpty()) {
			return idElement.getIdPart();
		}
		return null;
	}

	protected final String getIdentifier(String system, R resource) {
		try {
			Method getIdentifier = resource.getClass().getDeclaredMethod("getIdentifier");
			List<Identifier> identifiers = (List<Identifier>) getIdentifier.invoke(resource);
			for (Identifier identifier : identifiers) {
				if (StringUtils.equals(identifier.getSystem(), system)) {
					return identifier.getValue();
				}
			}
			return null;
		} catch (NoSuchMethodException | IllegalAccessException e) {
			// Die silently
			return null;
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	protected R getResourceByIdentifier(String identifierValue, String identifierSystem) throws JwkException, IOException {
		ICriterion<TokenClientParam> criterion = new TokenClientParam("identifier").exactly().systemAndIdentifier(identifierSystem, identifierValue);
		Bundle bundle = getFhirClient().search().forResource(getResourceName()).where(criterion).returnBundle(Bundle.class).execute();
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
