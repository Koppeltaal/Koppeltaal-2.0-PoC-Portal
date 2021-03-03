package nl.koppeltaal.poc.kt20.controllers;

import nl.koppeltaal.poc.fhir.dto.PatientDto;
import nl.koppeltaal.poc.fhir.service.PatientFhirClientService;
import nl.koppeltaal.poc.kt20.services.Kt20LaunchService;
import nl.koppeltaal.poc.kt20.valueobjects.LaunchData;
import nl.koppeltaal.poc.portal.controllers.SessionTokenStorage;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 *
 */
@Controller
@RequestMapping("/kt2")
public class Kt20LaunchController {
	final Kt20LaunchService kt20LaunchService;

	final PatientFhirClientService patientFhirClientService;

	public Kt20LaunchController(Kt20LaunchService kt20LaunchService, PatientFhirClientService patientFhirClientService) {
		this.kt20LaunchService = kt20LaunchService;
		this.patientFhirClientService = patientFhirClientService;
	}


	@RequestMapping(value = {"launch/ActivityDefinition/{treatmentId}/Patient/{patientId}", "launch/{treatmentId}"}, produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String launch(HttpSession session, @PathVariable("treatmentId") String treatmentId, @PathVariable("patientId") String patientId) throws Exception {
		SessionTokenStorage tokenStorage = new SessionTokenStorage(session);
		Object usr = session.getAttribute("user");
		RelatedPerson relatedPerson = null;
		Practitioner practitioner = null;
		Patient patient = null;
		if (usr instanceof Practitioner) {
			 practitioner = (Practitioner) usr;
		} else if (usr instanceof Patient) {
			 patient = (Patient) usr;
		} else  if (usr instanceof RelatedPerson) {
			relatedPerson = (RelatedPerson) usr;
		}

		if (patient == null && StringUtils.isNotEmpty(patientId)) {
			patient = patientFhirClientService.getResourceByReference(tokenStorage, patientId);
		}
		LaunchData launchData = new LaunchData("index.html", "", true);
		if (practitioner != null && patient != null) {
			launchData = kt20LaunchService.launchPractitioner(tokenStorage, practitioner, patient, treatmentId);
		} else if (relatedPerson != null && patient != null) {
			launchData = kt20LaunchService.launchRelatedPerson(tokenStorage, relatedPerson, patient, treatmentId);
		} else if (patient != null) {
			launchData = kt20LaunchService.launchPatient(tokenStorage, patient,  treatmentId);
		}
		String method = (launchData.isRedirect() ? "get" : "post");
		return "<html>\n" +
				"<head>\n" +
				"</head>\n" +
				"<body onload=\"document.forms[0].submit();\">\n" +
				"<form action=\"" + launchData.getUrl() + "\" method=\"" + method + "\">\n" +
				"<input type=\"hidden\" name=\"token\" value=\"" + encodeToken(launchData.getToken()) + "\"/>\n" +
				"</form>\n" +
				"</body>\n" +
				"</html>";
	}

	private String encodeToken(String token) throws UnsupportedEncodingException {
		return URLEncoder.encode(token, StandardCharsets.UTF_8.name());
	}

}
