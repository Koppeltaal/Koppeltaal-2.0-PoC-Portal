/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.dto;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public interface DtoConverter<D extends BaseDto, R extends DomainResource> {
	void applyDto(R resource, D dto);

	D convert(R resource);
	R convert(D dto);

	default void addTelecom(ContactPoint contactPoint, String homeEmail, ContactPoint.ContactPointUse use, ContactPoint.ContactPointSystem system) {
		setTelecom(homeEmail, use, system, contactPoint);
	}

	default void setTelecom(String homeEmail, ContactPoint.ContactPointUse use, ContactPoint.ContactPointSystem system, ContactPoint contactPoint) {
		contactPoint.setUse(use);
		contactPoint.setSystem(system);
		contactPoint.setValue(homeEmail);
	}

	default void addTelecomEmail(ContactPoint contactPoint, String homeEmail, ContactPoint.ContactPointUse use) {
		addTelecom(contactPoint, homeEmail, use, ContactPoint.ContactPointSystem.EMAIL);
	}

	default void addTelecomPhone(ContactPoint contactPoint, String homeEmail, ContactPoint.ContactPointUse use) {
		addTelecom(contactPoint, homeEmail, use, ContactPoint.ContactPointSystem.PHONE);
	}

	default Identifier createIdentifier(String system, String value) {
		Identifier identifier = new Identifier();
		Identifier.IdentifierUse use = Identifier.IdentifierUse.OFFICIAL;
		identifier.setSystem(system);
		identifier.setValue(value);
		identifier.setUse(use);
		return identifier;
	}

	default String joinAdressLines(Address address) {
		String addressLine = "";
		for (StringType stringType : address.getLine()) {
			if (StringUtils.isNotEmpty(addressLine)) {
				addressLine += "\n";
			}
			addressLine += stringType.getValue();
		}
		return addressLine;
	}

	default List<String> unjoinAdressLine(String addressLines) {
		return Arrays.asList(StringUtils.split(addressLines, "\n"));
	}

	default String getRelativeReference(IIdType idElement) {
		return idElement.getResourceType()  +"/" + idElement.toUnqualifiedVersionless().getIdPart();
	}

}
