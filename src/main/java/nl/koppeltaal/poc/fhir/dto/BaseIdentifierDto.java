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
public class BaseIdentifierDto extends BaseDto {
	String identifierValue;
	String identifierSystem;
	boolean active;

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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
