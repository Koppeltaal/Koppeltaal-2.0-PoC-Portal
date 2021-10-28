/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import static nl.koppeltaal.spring.boot.starter.smartservice.dto.TaskDtoConverter.KT2_PROFILE_EXTENSION__CARE_TEAM__OBSERVER;

import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.TokenParamModifier;
import com.auth0.jwk.JwkException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.TaskDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.TaskDtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.ActivityDefinitionFhirClientService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.CareTeamFhirClientService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PatientFhirClientService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.TaskFhirClientService;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Task;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 */
@RestController()
@RequestMapping("/api/Task")
public class TasksController extends BaseResourceController<TaskDto, Task> {


	final TaskFhirClientService fhirClientService;
	final PatientFhirClientService patientFhirClientService;
	final ActivityDefinitionFhirClientService activityDefinitionFhirClientService;
	final CareTeamFhirClientService careTeamService;
	final TaskDtoConverter dtoConverter;


	public TasksController(TaskFhirClientService fhirClientService,
			PatientFhirClientService patientFhirClientService,
			ActivityDefinitionFhirClientService activityDefinitionFhirClientService,
			CareTeamFhirClientService careTeamService,
			TaskDtoConverter dtoConverter) {
		super(fhirClientService, dtoConverter);
		this.fhirClientService = fhirClientService;
		this.patientFhirClientService = patientFhirClientService;
		this.activityDefinitionFhirClientService = activityDefinitionFhirClientService;
		this.careTeamService = careTeamService;
		this.dtoConverter = dtoConverter;
	}

	@RequestMapping(value = "ActivityDefinition/{activityDefinitionId}/Patient/{patientId}", method = RequestMethod.PUT)
	public TaskDto addNew(HttpSession httpSession, @PathVariable String activityDefinitionId, @PathVariable String patientId) throws IOException, JwkException {
		Object user = httpSession.getAttribute("user");
		Task task = new Task();
		if (user instanceof Practitioner) {
			Practitioner practitioner = (Practitioner) user;
			Patient patient = patientFhirClientService.getResourceByReference("Patient/" + patientId);
			ActivityDefinition activityDefinition = activityDefinitionFhirClientService.getResourceByReference("ActivityDefinition/" + activityDefinitionId);
			task = fhirClientService.getOrCreateTask(patient, practitioner, activityDefinition, true);
		} else if (user instanceof Patient) {
			Patient patient = (Patient) user;
			ActivityDefinition activityDefinition = activityDefinitionFhirClientService.getResourceByReference("ActivityDefinition/" + activityDefinitionId);
			task = fhirClientService.getOrCreateTask(patient, null, activityDefinition, true);
		}
		return dtoConverter.convert(task);
	}


	@RequestMapping(value = "Patient/{patientId}", method = RequestMethod.GET)
	public List<TaskDto> getForActivityDefinition(HttpSession httpSession, @PathVariable String patientId) throws IOException, JwkException {

		Object user = httpSession.getAttribute("user");

		List<TaskDto> rv = new ArrayList<>();

		final String targetPatientReference = "Patient/" + patientId;
		List<Task> list = fhirClientService.getResourcesByOwner(targetPatientReference);
		for (Task task : list) {
			rv.add(dtoConverter.convert(task));
		}

		// Patient is requesting their own tasks
		if(user instanceof Patient && StringUtils.equals(targetPatientReference, getReference((Patient) user))) {
			return rv;
		}

		final Map<String, List<IQueryParameterType>> criteria = new HashMap<>();

		final TokenParam participantParam = new TokenParam();
		participantParam.setModifier(TokenParamModifier.IN);
		participantParam.setValue(getReference(((DomainResource) user)));

		final TokenParam subjectParam = new TokenParam();
		subjectParam.setModifier(TokenParamModifier.TEXT);
		subjectParam.setValue(targetPatientReference);

		criteria.put("participant", Collections.singletonList(participantParam));
		criteria.put("subject", Collections.singletonList(subjectParam));

		//all careteams for the requested patient where the logged in user is a participant
		final List<String> requestingUserCareTeams = careTeamService.getResources(criteria).stream()
				.map(this::getReference)
				.collect(Collectors.toList());

		//TODO: Embed the careteams into the search criteria instead of filtering in code
		return rv.stream()
				.filter(taskDto -> CollectionUtils.containsAny(taskDto.getObserverReferences(), requestingUserCareTeams))
				.collect(Collectors.toList());

	}

	@PutMapping("setObserverTeams")
	public TaskDto setObserverTeams(HttpSession session, @RequestParam String taskReference, @RequestParam(required = false) List<String> careTeamReferences) {
		Task task = securityCheckSetCareTeam(session, taskReference);

		final List<Extension> extensionsToKeep = task.getExtensionsByUrl(KT2_PROFILE_EXTENSION__CARE_TEAM__OBSERVER).stream()
				.filter(extension -> !StringUtils.equals(KT2_PROFILE_EXTENSION__CARE_TEAM__OBSERVER, extension.getUrl()))
				.collect(Collectors.toList());
		task.setExtension(extensionsToKeep);

		if(careTeamReferences != null) {
			careTeamReferences.forEach(careTeamReference ->
					TaskDtoConverter.addObserverExtension(task, careTeamReference)
			);
		}

		try {
			return dtoConverter.convert(fhirClientService.storeResource(task));
		} catch (IOException e) {
			throw new RuntimeException("Something went wrong while assigning a Task.observer", e);
		}
	}

	private Task securityCheckSetCareTeam(HttpSession session, String taskReference) {
		Object user = session.getAttribute("user");
		if(!(user instanceof Patient)) throw new SecurityException("Not allowed to assign a CareTeam");
		Patient patient = (Patient) user;

		final Task resourceByReference = fhirClientService.getResourceByReference(taskReference);
		if(!StringUtils.equals(resourceByReference.getOwner().getReference(), getReference(patient))) {
			throw new SecurityException("Cannot assign CareTeams to someone else's Task");
		}

		return  resourceByReference;
	}


}
