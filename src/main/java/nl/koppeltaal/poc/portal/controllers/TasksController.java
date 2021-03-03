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
import nl.koppeltaal.poc.fhir.service.TaskFhirClientService;
import org.hl7.fhir.r4.model.Task;
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
	final TaskDtoConverter dtoConverter;

	public TasksController(TaskFhirClientService fhirClientService, TaskDtoConverter dtoConverter) {
		super(fhirClientService, dtoConverter);
		this.fhirClientService = fhirClientService;
		this.dtoConverter = dtoConverter;
	}

	@RequestMapping(value = "Patient/{reference}", method = RequestMethod.GET)
	public List<TaskDto> getForActivityDefinition(HttpSession httpSession, @PathVariable String reference) throws IOException, JwkException {
		List<TaskDto> rv = new ArrayList<>();
		List<Task> list = fhirClientService.getResourcesByOwner(new SessionTokenStorage(httpSession), "Patient/" + reference);
		for (Task activitydefinition : list) {
			rv.add(dtoConverter.convert(activitydefinition));
		}
		return rv;
	}


}
