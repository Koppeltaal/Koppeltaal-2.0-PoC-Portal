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

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Component
public class RelatedPersonDtoConverter implements DtoConverter<RelatedPersonDto, RelatedPerson> {

	public void applyDto(RelatedPerson relatedPerson, RelatedPersonDto relatedPersonDto) {
		setId(relatedPerson, relatedPersonDto);
		relatedPerson.setIdentifier(Collections.singletonList(createIdentifier(relatedPersonDto.getIdentifierSystem(), relatedPersonDto.getIdentifierValue())));

		relatedPerson.setActive(relatedPersonDto.isActive());

		// TODO: dangerous code...
		relatedPerson.getTelecom().clear();

		String email = relatedPersonDto.getEmail();
		if (StringUtils.isNotEmpty(email)) {
			addTelecomEmail(relatedPerson.addTelecom(), email, ContactPoint.ContactPointUse.WORK);
		}
		String phone = relatedPersonDto.getPhone();
		if (StringUtils.isNotEmpty(phone)) {
			addTelecomPhone(relatedPerson.addTelecom(), phone, ContactPoint.ContactPointUse.WORK);
		}

		// TODO: dangerous code...
		relatedPerson.getAddress().clear();

		String addressLines = relatedPersonDto.getAddressLines();
		String addressCity = relatedPersonDto.getAddressCity();
		String addressPostalCode = relatedPersonDto.getAddressPostalCode();
		String addressCountry = relatedPersonDto.getAddressCountry();

		if (!StringUtils.isAllEmpty(addressLines, addressCity, addressPostalCode, addressCountry)) {
			Address address = relatedPerson.addAddress();
			for (String line : unjoinAdressLine(addressLines)) {
				address.addLine(line);
			}
			address.setCity(addressCity);
			address.setPostalCode(addressPostalCode);
		}


		String gender = relatedPersonDto.getGender();
		if (StringUtils.isNotEmpty(gender)) {
			relatedPerson.setGender(Enumerations.AdministrativeGender.fromCode(gender));
		}

		relatedPerson.setBirthDate(relatedPersonDto.getBirthDate());

		relatedPerson.getName().clear();
		HumanName humanName = relatedPerson.addName();
		humanName.setFamily(relatedPersonDto.getNameFamily());
		if (StringUtils.isNotEmpty(relatedPersonDto.getNameGiven())) {
			humanName.getGiven().clear();
			for (String givenName : StringUtils.split(relatedPersonDto.getNameGiven())) {
				humanName.addGiven(givenName);
			}
		}

		if (StringUtils.isNotEmpty(relatedPersonDto.getRelationshipSystem()) && StringUtils.isNotEmpty(relatedPersonDto.getRelationshipCode())) {
			CodeableConcept codeableConcept = new CodeableConcept();
			Coding coding = new Coding(relatedPersonDto.getRelationshipSystem(), relatedPersonDto.getRelationshipCode(), null);
			codeableConcept.setCoding(Collections.singletonList(coding));
			relatedPerson.setRelationship(Collections.singletonList(codeableConcept));
		} else if (StringUtils.isNotEmpty(relatedPersonDto.getRelationship()) && StringUtils.contains(relatedPersonDto.getRelationship(), '|')) {
			String relationship = relatedPersonDto.getRelationship();
			String[] parts = StringUtils.split(relationship, '|');
			if (parts.length > 1) {
				CodeableConcept codeableConcept = new CodeableConcept();
				Coding coding = new Coding(parts[0], parts[1], null);
				codeableConcept.setCoding(Collections.singletonList(coding));
				relatedPerson.setRelationship(Collections.singletonList(codeableConcept));
			}
		} else {
			relatedPerson.getRelationship().clear();
		}

		String patient = relatedPersonDto.getPatient();
		if (StringUtils.isNotEmpty(patient)) {
			relatedPerson.setPatient(new Reference(patient));
		} else {
			relatedPerson.setPatient(null);
		}
	}

	@SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
	public void applyResource(RelatedPersonDto relatedPersonDto, RelatedPerson relatedPerson) {

		relatedPersonDto.setReference(getRelativeReference(relatedPerson.getIdElement()));

		List<Identifier> identifiers = relatedPerson.getIdentifier();
		for (Identifier identifier : identifiers) {
			relatedPersonDto.setIdentifierSystem(identifier.getSystem());
			relatedPersonDto.setIdentifierValue(identifier.getValue());
		}

		relatedPersonDto.setActive(relatedPerson.getActive());
		List<ContactPoint> telecoms = relatedPerson.getTelecom();
		for (ContactPoint telecom : telecoms) {
			if (StringUtils.equals(telecom.getSystem().toCode(), "email")
					&& StringUtils.equals(telecom.getUse().toCode(), "work")) {
				relatedPersonDto.setEmail(telecom.getValue());
			}
			if (StringUtils.equals(telecom.getSystem().toCode(), "phone")
					&& StringUtils.equals(telecom.getUse().toCode(), "work")) {
				relatedPersonDto.setPhone(telecom.getValue());
			}
		}

		for (Address address : relatedPerson.getAddress()) {
			relatedPersonDto.setAddressLines(joinAddressLines(address));
			relatedPersonDto.setAddressCity(address.getCity());
			relatedPersonDto.setAddressPostalCode(address.getPostalCode());
			relatedPersonDto.setAddressCountry(address.getCountry());
			break;
		}

		Enumerations.AdministrativeGender gender = relatedPerson.getGender();
		if (gender != null) {
			relatedPersonDto.setGender(gender.toCode());
		} else {
			relatedPersonDto.setGender(Enumerations.AdministrativeGender.UNKNOWN.toCode());
		}


		Date birthDate = relatedPerson.getBirthDate();
		relatedPersonDto.setBirthDate(birthDate);

		for (HumanName humanName : relatedPerson.getName()) {
			relatedPersonDto.setNameFamily(humanName.getFamily());
			relatedPersonDto.setNameGiven(humanName.getGivenAsSingleString());
			break;
		}

		for (CodeableConcept codeableConcept : relatedPerson.getRelationship()) {
			for (Coding coding : codeableConcept.getCoding()) {
				relatedPersonDto.setRelationshipSystem(coding.getSystem());
				relatedPersonDto.setRelationshipCode(coding.getCode());
				relatedPersonDto.setRelationship(String.format("%s|%s", coding.getSystem(), coding.getCode()));
				break;
			}
		}

		Reference patient = relatedPerson.getPatient();
		if (patient != null) {
			relatedPersonDto.setPatient(patient.getReference());
		}

	}

	public RelatedPersonDto convert(RelatedPerson relatedPerson) {
		RelatedPersonDto relatedPersonDto = new RelatedPersonDto();

		applyResource(relatedPersonDto, relatedPerson);


		return relatedPersonDto;
	}

	public RelatedPerson convert(RelatedPersonDto relatedPersonDto) {
		RelatedPerson relatedPerson = new RelatedPerson();

		applyDto(relatedPerson, relatedPersonDto);
		return relatedPerson;
	}

}
