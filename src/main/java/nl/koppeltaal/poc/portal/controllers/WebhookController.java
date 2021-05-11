package nl.koppeltaal.poc.portal.controllers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import nl.koppeltaal.poc.portal.service.EmailService;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("webhook")
public class WebhookController {

  private final EmailService emailService;

  private final IParser jsonParser = FhirContext.forR4().newJsonParser();

  public WebhookController(EmailService emailService) {
    this.emailService = emailService;
  }

  @PutMapping(value = "patient/**", consumes = "application/fhir+json;charset=UTF-8")
  public void patientWebhook(@RequestBody String patientPayload) {

    final Patient patient = jsonParser.parseResource(Patient.class, patientPayload);

    emailService.sendWelcomeEmail(patient);
  }


}
