package nl.koppeltaal.poc.portal.filter;

import nl.koppeltaal.spring.boot.starter.smartservice.service.context.TraceContext;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PatientFhirClientService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PractitionerFhirClientService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.RelatedPersonFhirClientService;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AddFhirUserToSessionFilter extends GenericFilterBean {

    final PatientFhirClientService patientFhirClientService;
    final PractitionerFhirClientService practitionerFhirClientService;
    final RelatedPersonFhirClientService relatedPersonFhirClientService;

    public AddFhirUserToSessionFilter(PatientFhirClientService patientFhirClientService, PractitionerFhirClientService practitionerFhirClientService, RelatedPersonFhirClientService relatedPersonFhirClientService) {
        this.patientFhirClientService = patientFhirClientService;
        this.practitionerFhirClientService = practitionerFhirClientService;
        this.relatedPersonFhirClientService = relatedPersonFhirClientService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpSession session = ((HttpServletRequest) request).getSession();
        Object user = session.getAttribute("user");

        if(user != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String keycloakSecurityContext = getPreferredUsername();
        if(keycloakSecurityContext != null) {
            addFhirUserToSession(keycloakSecurityContext, session);
        }

        filterChain.doFilter(request, response);
    }

    private String getPreferredUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof OidcUser) {
                return OidcUser.class.cast(principal).getPreferredUsername();
            }
        }

        return null;
    }

    private void addFhirUserToSession(String username, HttpSession session) {

        TraceContext traceContext = new TraceContext();
        Practitioner practitioner = practitionerFhirClientService.getResourceByIdentifier(username, traceContext);
        Patient patient = patientFhirClientService.getResourceByIdentifier(username, traceContext);

        if (practitioner != null) {
            session.setAttribute("user", practitioner);
        } else if (patient != null) {
            session.setAttribute("user", patient);
        }
    }
}
