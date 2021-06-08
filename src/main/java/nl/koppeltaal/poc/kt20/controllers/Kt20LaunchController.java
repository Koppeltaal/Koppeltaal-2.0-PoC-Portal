package nl.koppeltaal.poc.kt20.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpSession;
import nl.koppeltaal.poc.kt20.services.Kt20LaunchService;
import nl.koppeltaal.poc.kt20.valueobjects.LaunchData;
import nl.koppeltaal.poc.portal.controllers.SessionTokenStorage;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PatientFhirClientService;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.RelatedPerson;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

	@RequestMapping(value = {"launch/Task/{taskId}"}, produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String launchTask(HttpSession session, @PathVariable("taskId") String taskId, boolean isNew) throws Exception {
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
		LaunchData launchData = new LaunchData("index.html", "", true);
		if (practitioner != null) {
			launchData = kt20LaunchService.launchTaskPractitioner(practitioner, taskId);
		} else if (relatedPerson != null) {
			launchData = kt20LaunchService.launchTaskRelatedPerson(tokenStorage, relatedPerson, taskId);
		} else if (patient != null) {
			launchData = kt20LaunchService.launchTaskPatient(patient, taskId);
		}
		return renderLaunchData(launchData);
	}


	@RequestMapping(value = {"launch/ActivityDefinition/{treatmentId}/Patient/{patientId}", "launch/{treatmentId}"}, produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String launch(HttpSession session, @PathVariable("treatmentId") String treatmentId, @PathVariable("patientId") String patientId, @RequestParam(value = "new", required = false) boolean isNew) throws Exception {
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
			patient = patientFhirClientService.getResourceByReference(patientId);
		}
		LaunchData launchData = new LaunchData("index.html", "", true);
		if (practitioner != null && patient != null) {
			launchData = kt20LaunchService.launchPractitioner(practitioner, patient, treatmentId, isNew);
		} else if (relatedPerson != null && patient != null) {
			launchData = kt20LaunchService.launchRelatedPerson(relatedPerson, patient, treatmentId, isNew);
		} else if (patient != null) {
			launchData = kt20LaunchService.launchPatient(patient,  treatmentId, isNew);
		}
		return renderLaunchData(launchData);
	}

	private String renderLaunchData(LaunchData launchData) throws UnsupportedEncodingException {
		String method = launchData.isRedirect() ? "get" : "post";
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
