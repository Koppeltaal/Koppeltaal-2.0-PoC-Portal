/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.controllers;

import com.auth0.jwk.JwkException;
import nl.koppeltaal.poc.fhir.dto.TaskDto;
import nl.koppeltaal.poc.fhir.dto.TaskDtoConverter;
import nl.koppeltaal.poc.fhir.service.ActivityDefinitionFhirClientService;
import nl.koppeltaal.poc.fhir.service.PatientFhirClientService;
import nl.koppeltaal.poc.fhir.service.TaskFhirClientService;
import org.hl7.fhir.r4.model.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@RestController()
@RequestMapping("/api/Task")
public class TasksController extends BaseResourceController<TaskDto, Task> {


	final TaskFhirClientService fhirClientService;
	final PatientFhirClientService patientFhirClientService;
	final ActivityDefinitionFhirClientService activityDefinitionFhirClientService;
	final TaskDtoConverter dtoConverter;


	public TasksController(TaskFhirClientService fhirClientService, PatientFhirClientService patientFhirClientService, ActivityDefinitionFhirClientService activityDefinitionFhirClientService, TaskDtoConverter dtoConverter) {
		super(fhirClientService, dtoConverter);
		this.fhirClientService = fhirClientService;
		this.patientFhirClientService = patientFhirClientService;
		this.activityDefinitionFhirClientService = activityDefinitionFhirClientService;
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
		List<TaskDto> rv = new ArrayList<>();
		List<Task> list = fhirClientService.getResourcesByOwner("Patient/" + patientId);
		for (Task task : list) {
			rv.add(dtoConverter.convert(task));
		}
		return rv;
	}


}
