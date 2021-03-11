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
import java.util.List;

/**
 *
 */
@Component
public class OrganizationDtoConverter implements DtoConverter<OrganizationDto, Organization> {


	public void applyDto(Organization organization, OrganizationDto organizationDto) {
		setId(organization, organizationDto);
		organization.setIdentifier(Collections.singletonList(createIdentifier(organizationDto.getIdentifierSystem(), organizationDto.getIdentifierValue())));

		organization.setActive(organizationDto.isActive());

		// TODO: dangerous code...
		organization.getTelecom().clear();

		String email = organizationDto.getEmail();
		if (StringUtils.isNotEmpty(email)) {
			addTelecomEmail(organization.addTelecom(), email, ContactPoint.ContactPointUse.WORK);
		}
		String phone = organizationDto.getPhone();
		if (StringUtils.isNotEmpty(phone)) {
			addTelecomPhone(organization.addTelecom(), phone, ContactPoint.ContactPointUse.WORK);
		}

		// TODO: dangerous code...
		organization.getAddress().clear();

		String addressLines = organizationDto.getAddressLines();
		String addressCity = organizationDto.getAddressCity();
		String addressPostalCode = organizationDto.getAddressPostalCode();
		String addressCountry = organizationDto.getAddressCountry();

		if (!StringUtils.isAllEmpty(addressLines, addressCity, addressPostalCode, addressCountry)) {
			Address address = organization.addAddress();
			for (String line : unjoinAdressLine(addressLines)) {
				address.addLine(line);
			}
			address.setCity(addressCity);
			address.setPostalCode(addressPostalCode);
		}

		organization.setName(organizationDto.getName());

		organization.getType().clear();
		String type = organizationDto.getType();
		if (StringUtils.isNotEmpty(type)) {
			CodeableConcept codeableConcept = organization.addType();
			codeableConcept.getCoding().clear();
			Coding coding = codeableConcept.addCoding();
			coding.setSystem("http://terminology.hl7.org/CodeSystem/organization-type");
			coding.setCode(type);
		}


	}

	@SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
	public void applyResource(OrganizationDto organizationDto, Organization organization) {
		organizationDto.setReference(getRelativeReference(organization.getIdElement()));

		List<Identifier> identifiers = organization.getIdentifier();
		for (Identifier identifier : identifiers) {
			organizationDto.setIdentifierSystem(identifier.getSystem());
			organizationDto.setIdentifierValue(identifier.getValue());
		}

		organizationDto.setActive(organization.getActive());

		List<ContactPoint> telecoms = organization.getTelecom();
		for (ContactPoint telecom : telecoms) {
			if (StringUtils.equals(telecom.getSystem().toCode(), "email")) {
				if (StringUtils.equals(telecom.getUse().toCode(), "work")) {
					organizationDto.setEmail(telecom.getValue());
				}
			} else if (StringUtils.equals(telecom.getSystem().toCode(), "phone")
					&& StringUtils.equals(telecom.getUse().toCode(), "work")) {
				organizationDto.setPhone(telecom.getValue());
			}
		}

		for (Address address : organization.getAddress()) {
			organizationDto.setAddressLines(joinAddressLines(address));
			organizationDto.setAddressCity(address.getCity());
			organizationDto.setAddressPostalCode(address.getPostalCode());
			organizationDto.setAddressCountry(address.getCountry());
			break;
		}
		for (CodeableConcept codeableConcept : organization.getType()) {
			for (Coding coding : codeableConcept.getCoding()) {
				if (StringUtils.equals(coding.getSystem(), "http://terminology.hl7.org/CodeSystem/organization-type")) {
					organizationDto.setType(coding.getCode());
					break;
				}
			}
		}

		organizationDto.setName(organization.getName());


	}

	public OrganizationDto convert(Organization organization) {
		OrganizationDto organizationDto = new OrganizationDto();

		applyResource(organizationDto, organization);


		return organizationDto;
	}

	public Organization convert(OrganizationDto organizationDto) {
		Organization organization = new Organization();

		applyDto(organization, organizationDto);
		return organization;
	}


}
