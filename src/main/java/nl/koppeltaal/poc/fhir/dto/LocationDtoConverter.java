/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.dto;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
public class LocationDtoConverter implements DtoConverter<LocationDto, Location> {

	public void applyDto(Location location, LocationDto locationDto) {
		setId(location, locationDto);
		location.getIdentifier().clear();
		location.getEndpoint().clear();
		Reference reference = new Reference();
		reference.setType("Endpoint");
		Identifier identifier = createIdentifier("urn:ietf:rfc:3986", locationDto.getAddress());
		reference.setIdentifier(identifier);
		location.addEndpoint(reference);
		location.addIdentifier(identifier);
	}

	public void applyResource(LocationDto locationDto, Location location) {
		locationDto.setReference(getRelativeReference(location.getIdElement()));
		List<Reference> endpoint = location.getEndpoint();
		for (Reference reference : endpoint) {
			if (StringUtils.equals("Endpoint", reference.getType())) {
				Identifier identifier = reference.getIdentifier();
				if (identifier != null && StringUtils.equals("urn:ietf:rfc:3986", identifier.getSystem())){
					locationDto.setAddress(identifier.getValue());
					break;
				}
			}

		}
	}

	public LocationDto convert(Location location) {
		LocationDto locationDto = new LocationDto();
		applyResource(locationDto, location);
		return locationDto;
	}

	public Location convert(LocationDto locationDto) {
		Location location = new Location();
		applyDto(location, locationDto);
		return location;
	}

}
