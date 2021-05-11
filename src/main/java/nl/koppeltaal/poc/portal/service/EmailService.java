package nl.koppeltaal.poc.portal.service;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service that uses spring-boot-starter-mail. This is currently used to send a very basic
 * welcome email to new Patients
 *
 * Please make sure to set all the `spring.mail.x` properties
 */
@Service
public class EmailService {

  private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);
  private final JavaMailSender javaMailSender;
  private  final String fromAddress;
  private final boolean serviceEnabled;

  public EmailService(Optional<JavaMailSender> javaMailSenderOptional, @Value("${kt20.email.from:}") String fromAddress, @Value("${kt20.email.enabled:false}") boolean serviceEnabled) {
    this.javaMailSender = serviceEnabled ? javaMailSenderOptional.orElseThrow(() -> new IllegalStateException("Email service is enabled but no JavaMailSender bean found - please make sure the `spring.mail` is properly configured")) : null;
    this.fromAddress = fromAddress;
    this.serviceEnabled = serviceEnabled;
  }

  /**
   * This function sends an email to the provided {@link Patient}.
   * The email will be sent to the first telecom op system "email" and use "home".
   *
   * @param patient
   */
  public void sendWelcomeEmail(Patient patient) {

    final Optional<ContactPoint> emailAddressOptional = validateSendAndGetEmailAddress(patient);

    if (!emailAddressOptional.isPresent()) return;

    final String emailAddress = emailAddressOptional.get().getValue();

    LOG.debug("Attempting to send a welcome email to [{}]", emailAddress);
    javaMailSender.send(
        getWelcomeMail(patient, emailAddress)
    );
    LOG.info("Successfully sent a welcome email to [{}]", emailAddress);
  }

  private Optional<ContactPoint> validateSendAndGetEmailAddress(Patient patient) {
    if(!serviceEnabled) {
      LOG.warn("Not sending welcome email for new patient [{}], the email service is disabled", patient
          .getId());
      return Optional.empty();
    }

    //Only send emails to new patients
    if(!"1".equals(patient.getMeta().getVersionId())) {
      LOG.debug("Not sending welcome email for patient [{}], not a new patient", patient.getId());
      return Optional.empty();
    }

    final Optional<ContactPoint> emailAddressOptional = patient.getTelecom().stream()
        .filter(telecom -> telecom.getUse() == ContactPointUse.HOME
            && telecom.getSystem() == ContactPointSystem.EMAIL
            && !StringUtils.isBlank(telecom.getValue()))
        .findFirst();

    if(!emailAddressOptional.isPresent()) {
      LOG.warn("Cannot send welcome email for new patient [{}], there is no home email address provided.", patient
          .getId());

      return Optional.empty();
    }
    return emailAddressOptional;
  }

  private SimpleMailMessage getWelcomeMail(Patient patient, String emailAddress) {
    SimpleMailMessage msg = new SimpleMailMessage();

    msg.setTo(emailAddress);
    msg.setFrom(fromAddress);
    msg.setSubject("Welkom op het portaal!");

    final String surname = patient.getNameFirstRep() != null ? " " + patient.getNameFirstRep().getFamily() : "";

    msg.setText(String.format("Beste%s,\n\n Welkom op het portaal, wat goed dat je er bent!", surname));

    return msg;
  }
}
