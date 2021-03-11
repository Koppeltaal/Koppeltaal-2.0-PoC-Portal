/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.dto;

import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class TaskDtoConverter implements DtoConverter<TaskDto, Task> {

	public void applyDto(Task task, TaskDto taskDto) {
		setId(task, taskDto);
		task.setRequester(new Reference(taskDto.getPractitioner()));
		task.setOwner(new Reference(taskDto.getPatient()));
		task.setInstantiatesCanonical(taskDto.getActivityDefinition());
		task.setStatus(Task.TaskStatus.fromCode(taskDto.getStatus()));
	}

	public void applyResource(TaskDto taskDto, Task task) {
		taskDto.setReference(getRelativeReference(task.getIdElement()));
		taskDto.setPatient(task.getOwner().getReference());
		taskDto.setPractitioner(task.getRequester().getReference());
		taskDto.setActivityDefinition(task.getInstantiatesCanonical());
		if (task.getStatus() != null)
			taskDto.setStatus(task.getStatus().toCode());
	}

	public TaskDto convert(Task task) {
		TaskDto taskDto = new TaskDto();
		applyResource(taskDto, task);
		return taskDto;
	}

	public Task convert(TaskDto taskDto) {
		Task task = new Task();
		applyDto(task, taskDto);
		return task;
	}

}
