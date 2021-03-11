/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package nl.koppeltaal.poc.fhir.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class EnitityNotFoundException extends RuntimeException {
	public EnitityNotFoundException() {
	}

	public EnitityNotFoundException(String message) {
		super(message);
	}

	public EnitityNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public EnitityNotFoundException(Throwable cause) {
		super(cause);
	}

	public EnitityNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
