/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.dto;

import org.hl7.fhir.r4.model.Endpoint;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 *
 */
@Component
public class EndpointDtoConverter implements DtoConverter<EndpointDto, Endpoint> {

	public void applyDto(Endpoint endpoint, EndpointDto endpointDto) {
		setId(endpoint, endpointDto);
		endpoint.setIdentifier(Collections.singletonList(createIdentifier("urn:ietf:rfc:3986", endpointDto.getAddress())));
		// TODO: implement the rest
		endpoint.setAddress(endpointDto.getAddress());
		endpoint.setName(endpointDto.getName());
		endpoint.setStatus(Endpoint.EndpointStatus.fromCode(endpointDto.getStatus()));
	}



	public void applyResource(EndpointDto endpointDto, Endpoint endpoint) {
		endpointDto.setReference(getRelativeReference(endpoint.getIdElement()));
		endpointDto.setAddress(endpoint.getAddress());
		endpointDto.setName(endpoint.getName());
		endpointDto.setStatus(endpoint.getStatus().toCode());


	}

	public EndpointDto convert(Endpoint endpoint) {
		EndpointDto endpointDto = new EndpointDto();

		applyResource(endpointDto, endpoint);


		return endpointDto;
	}

	public Endpoint convert(EndpointDto endpointDto) {
		Endpoint endpoint = new Endpoint();

		applyDto(endpoint, endpointDto);
		return endpoint;
	}


}
