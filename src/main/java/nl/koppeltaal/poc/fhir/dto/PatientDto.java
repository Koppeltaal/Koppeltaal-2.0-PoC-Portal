/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.dto;

/**
 *
 */
public class PatientDto extends PersonDto {
	String workEmail;
	String homeEmail;

	public String getHomeEmail() {
		return homeEmail;
	}

	public void setHomeEmail(String homeEmail) {
		this.homeEmail = homeEmail;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getIdentifierSystem() {
		return identifierSystem;
	}

	public void setIdentifierSystem(String identifierSystem) {
		this.identifierSystem = identifierSystem;
	}

	public String getIdentifierValue() {
		return identifierValue;
	}

	public void setIdentifierValue(String identifierValue) {
		this.identifierValue = identifierValue;
	}

	public String getWorkEmail() {
		return workEmail;
	}

	public void setWorkEmail(String workEmail) {
		this.workEmail = workEmail;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
