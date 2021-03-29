/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.dto;

import java.util.Date;

/**
 *
 */
@SuppressWarnings("unused")
public class PersonDto extends BaseIdentifierDto {
	String nameFamily;
	String nameGiven;
	String gender;
	Date birthDate;

	public Date getBirthDate() {
		return birthDate == null ? null : new Date(birthDate.getTime());
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate == null ? null : new Date(birthDate.getTime());
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
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


}
