/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.dto;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 *
 */
@Component
public class PatientDtoConverter implements DtoConverter<PatientDto, Patient> {

	public void applyDto(Patient patient, PatientDto patientDto) {
		patient.addIdentifier(createIdentifier(patientDto.getIdentifierSystem(), patientDto.getIdentifierValue()));

		patient.setActive(patientDto.isActive());

		// TODO: dangerous code...
		patient.getTelecom().clear();
		String workEmail = patientDto.getWorkEmail();
		if (StringUtils.isNotEmpty(workEmail)) {
			addTelecomEmail(patient.addTelecom(), workEmail, ContactPoint.ContactPointUse.WORK);

		}

		String homeEmail = patientDto.getHomeEmail();
		if (StringUtils.isNotEmpty(homeEmail)) {
			addTelecomEmail(patient.addTelecom(), homeEmail, ContactPoint.ContactPointUse.HOME);
		}

		String gender = patientDto.getGender();
		if (StringUtils.isNotEmpty(gender)) {
			patient.setGender(Enumerations.AdministrativeGender.fromCode(gender));
		}

		patient.setBirthDate(patientDto.getBirthDate());

		patient.getName().clear();
		HumanName humanName = patient.addName();
		humanName.setFamily(patientDto.getNameFamily());
		if (StringUtils.isNotEmpty(patientDto.getNameGiven())) {
			humanName.getGiven().clear();
			for (String givenName : StringUtils.split(patientDto.getNameGiven())) {
				humanName.addGiven(givenName);
			}
		}

		String organization = patientDto.getOrganization();
		if (StringUtils.isNotEmpty(organization) && StringUtils.contains(organization, "|")) {
			Reference managingOrganization = patient.getManagingOrganization();
			if (managingOrganization != null) {
				Identifier identifier = managingOrganization.getIdentifier();
				String[] split = StringUtils.split(organization, "|");
				if (split.length == 2) {
					identifier.setSystem(split[0]);
					identifier.setValue(split[1]);
				}
			}
		}
	}

	public void applyResource(PatientDto patientDto, Patient patient) {
		patientDto.setReference(getRelativeReference(patient.getIdElement()));

		List<Identifier> identifiers = patient.getIdentifier();
		for (Identifier identifier : identifiers) {
			patientDto.setIdentifierSystem(identifier.getSystem());
			patientDto.setIdentifierValue(identifier.getValue());
		}

		patientDto.setActive(patient.getActive());
		List<ContactPoint> telecoms = patient.getTelecom();
		for (ContactPoint telecom : telecoms) {
			if (StringUtils.equals(telecom.getSystem().toCode(), "email")) {
				if (StringUtils.equals(telecom.getUse().toCode(), "home")) {
					patientDto.setHomeEmail(telecom.getValue());
				}
				if (StringUtils.equals(telecom.getUse().toCode(), "work")) {
					patientDto.setWorkEmail(telecom.getValue());
				}
			}
		}

		Enumerations.AdministrativeGender gender = patient.getGender();
		if (gender != null) {
			patientDto.setGender(gender.toCode());
		} else {
			patientDto.setGender(Enumerations.AdministrativeGender.UNKNOWN.toCode());
		}


		Date birthDate = patient.getBirthDate();
		patientDto.setBirthDate(birthDate);

		for (HumanName humanName : patient.getName()) {
			patientDto.setNameFamily(humanName.getFamily());
			patientDto.setNameGiven(humanName.getGivenAsSingleString());
			break;
		}

		Reference organization = patient.getManagingOrganization();
		if (organization != null) {
			Identifier identifier = organization.getIdentifier();
			if (identifier != null) {
				patientDto.setOrganization(String.format("%s|%s", identifier.getSystem(), identifier.getValue()));
			}
		}

	}

	public PatientDto convert(Patient patient) {
		PatientDto patientDto = new PatientDto();

		applyResource(patientDto, patient);


		return patientDto;
	}

	public Patient convert(PatientDto patientDto) {
		Patient patient = new Patient();

		applyDto(patient, patientDto);
		return patient;
	}

}
