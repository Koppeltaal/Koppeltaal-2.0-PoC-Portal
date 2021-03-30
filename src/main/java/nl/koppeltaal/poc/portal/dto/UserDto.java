/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.portal.dto;

import nl.koppeltaal.poc.fhir.dto.PatientDto;

/**
 *
 */
public class UserDto {
	PatientDto patient;
	private String userId;
	private String nameGiven;
	private String nameFamily;
	private boolean loggedIn;
	private String userIdentifier;
	private String type;

	public boolean getLoggedIn() {
		return loggedIn;
	}

	public String getType() {
		return type;
	}

	public String getUserIdentifier() {
		return userIdentifier;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getNameFamily() {
		return nameFamily;
	}

	public void setNameFamily(String nameFamily) {
		this.nameFamily = nameFamily;
	}

	public String getNameGiven() {
		return nameGiven;
	}

	public void setNameGiven(String nameGiven) {
		this.nameGiven = nameGiven;
	}

	public PatientDto getPatient() {
		return patient;
	}

	public void setPatient(PatientDto patient) {
		this.patient = patient;
	}

	public String getUserId() {
		return userId;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setUserIdentifier(String userIdentifier) {
		this.userIdentifier = userIdentifier;
	}
}
