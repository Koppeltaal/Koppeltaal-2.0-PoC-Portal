/*
 * Copyright (c) Stichting Koppeltaal 2021.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package nl.koppeltaal.poc.utils;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class UrlUtils {
	public static boolean isDefault(String scheme, int serverPort) {
		return (StringUtils.equals("http", scheme) && serverPort == 80) || (StringUtils.equals("https", scheme) && serverPort == 443);
	}

	public static String getServerUrl(String path, HttpServletRequest servletRequest) {
		int serverPort = servletRequest.getServerPort();
		String scheme = servletRequest.getScheme();
		if (isDefault(scheme, serverPort)) {
			return scheme + "://" + servletRequest.getServerName() + path;
		} else {
			return scheme + "://" + servletRequest.getServerName() + ":" + serverPort + path;
		}
	}
}
