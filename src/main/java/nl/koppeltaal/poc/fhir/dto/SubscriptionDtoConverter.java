/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.dto;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Subscription;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
public class SubscriptionDtoConverter implements DtoConverter<SubscriptionDto, Subscription> {

	public void applyDto(Subscription subscription, SubscriptionDto subscriptionDto) {

		setId(subscription, subscriptionDto);
		subscription.setStatus(subscriptionDto.getStatus());
		subscription.setCriteria(subscriptionDto.getCriteria());
		subscription.setChannel(getChannel(subscriptionDto));
	}

	public SubscriptionDto convert(Subscription subscription) {
		SubscriptionDto subscriptionDto = new SubscriptionDto();

		subscriptionDto.setReference(getRelativeReference(subscription.getIdElement()));

		subscriptionDto.setStatus(subscription.getStatus());
		subscriptionDto.setCriteria(subscription.getCriteria());

		final Subscription.SubscriptionChannelComponent channel = subscription.getChannel();

		if(channel != null) {
			subscriptionDto.setEndpoint(channel.getEndpoint());
			subscriptionDto.setPayload(
					channel.getPayload() == null ? "NULL" : channel.getPayload()
			);

			subscriptionDto.setType(channel.getType());

			//TODO: NTH: Support multiple headers
			final List<StringType> header = channel.getHeader();
			if(header.size() == 1) {
				subscriptionDto.setHeader(header.get(0).getValue());
			}
		}

		return subscriptionDto;
	}

	public Subscription convert(SubscriptionDto subscriptionDto) {
		Subscription subscription = new Subscription();

		applyDto(subscription, subscriptionDto);

		return subscription;
	}

	private Subscription.SubscriptionChannelComponent getChannel(SubscriptionDto subscriptionDto) {

		final Subscription.SubscriptionChannelComponent channel = new Subscription.SubscriptionChannelComponent();
		channel.setEndpoint(subscriptionDto.getEndpoint());
		channel.addHeader(subscriptionDto.getHeader());
		channel.setType(subscriptionDto.getType());

		if(!StringUtils.equals("NULL", subscriptionDto.getPayload())) {
			channel.setPayload(subscriptionDto.getPayload());
		}

		return channel;
	}

}
