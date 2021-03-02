/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import com.auth0.jwk.JwkException;
import nl.koppeltaal.poc.fhir.configuration.FhirClientConfiguration;
import nl.koppeltaal.poc.fhir.dto.TaskDto;
import nl.koppeltaal.poc.fhir.dto.TaskDtoConverter;
import nl.koppeltaal.poc.generic.TokenStorage;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
public class TaskFhirClientService extends BaseFhirClientService<TaskDto, Task> {

	public TaskFhirClientService(FhirClientConfiguration fhirClientConfiguration, Oauth2ClientService oauth2ClientService, FhirContext fhirContext, TaskDtoConverter taskDtoConverter) {
		super(fhirClientConfiguration, oauth2ClientService, fhirContext, taskDtoConverter);
	}

	public Task getOrCreateTask(TokenStorage tokenStorage, Patient patient, Practitioner practitioner, ActivityDefinition activityDefinition) throws IOException, JwkException {
		List<Task> tasks = getTasksForOwnerAndDefinition(tokenStorage, patient, activityDefinition);
		Task task;
		if (tasks.isEmpty()) {
			task = new Task();
			task.setOwner(new Reference(patient));
			if (practitioner != null) {
				task.setRequester(new Reference(practitioner));
			}
			task.setStatus(Task.TaskStatus.READY);
			task.setIntent(Task.TaskIntent.ORDER);
			task.getRestriction().addRecipient(new Reference(practitioner));
			task.getExecutionPeriod().setStart(new Date());
		} else {
			task = tasks.get(0);
		}
		return task;
	}

	public List<Task> getResourcesByOwner(TokenStorage tokenStorage, String ownerReference) throws IOException, JwkException {
		List<Task> rv = new ArrayList<>();
		ICriterion<TokenClientParam> instantiatesCanonical = new TokenClientParam("owner").exactly().identifier(ownerReference);
		Bundle bundle = getFhirClient(tokenStorage).search().forResource(getResourceName()).where(instantiatesCanonical).returnBundle(Bundle.class).execute();
		for (Bundle.BundleEntryComponent component : bundle.getEntry()) {
			Task resource = (Task) component.getResource();
			rv.add(resource);
		}
		return rv;
	}

	protected String getDefaultSystem() {
		return "system";
	}

	protected String getIdentifier(String system, Task resource) {
		for (Identifier identifier : resource.getIdentifier()) {
			if (StringUtils.equals(identifier.getSystem(), system)) {
				return identifier.getValue();
			}
		}
		return null;
	}

	@Override
	protected String getResourceName() {
		return "Task";
	}

	private List<Task> getTasksForOwnerAndDefinition(TokenStorage tokenStorage, Patient fhirPatient, ActivityDefinition fhirDefinition) throws IOException, JwkException {
		List<Task> rv = new ArrayList<>();
		List<Task> resourcesByOwner = getResourcesByOwner(tokenStorage, fhirPatient.getId());
		for (Task task : resourcesByOwner) {
			if (StringUtils.equals(task.getInstantiatesCanonical(), fhirDefinition.getId())) {
				rv.add(task);
			}
		}
		return rv;
	}


}
