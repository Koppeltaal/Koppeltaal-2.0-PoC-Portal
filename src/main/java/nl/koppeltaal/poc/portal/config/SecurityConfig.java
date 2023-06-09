package nl.koppeltaal.poc.portal.config;

import nl.koppeltaal.poc.portal.filter.AddFhirUserToSessionFilter;
import nl.koppeltaal.poc.portal.handler.KeycloakLogoutHandler;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PatientFhirClientService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PractitionerFhirClientService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.RelatedPersonFhirClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationCodeGrantFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
class SecurityConfig {

    final PatientFhirClientService patientFhirClientService;
    final PractitionerFhirClientService practitionerFhirClientService;
    final RelatedPersonFhirClientService relatedPersonFhirClientService;
    private final KeycloakLogoutHandler keycloakLogoutHandler;

    SecurityConfig(PatientFhirClientService patientFhirClientService, PractitionerFhirClientService practitionerFhirClientService, RelatedPersonFhirClientService relatedPersonFhirClientService, KeycloakLogoutHandler keycloakLogoutHandler) {
        this.patientFhirClientService = patientFhirClientService;
        this.practitionerFhirClientService = practitionerFhirClientService;
        this.relatedPersonFhirClientService = relatedPersonFhirClientService;
        this.keycloakLogoutHandler = keycloakLogoutHandler;
    }

    // Specifies the session authentication strategy
    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf()
                    .disable()
                .authorizeRequests()
                .antMatchers("/.well-known/*")
                    .permitAll()
                .anyRequest()
                    .hasAnyRole("USER", "PATIENT", "PRACTITIONER", "RELATEDPERSON")
                .and()
                    .addFilterAfter(addFhirUserToSessionFilter(), OAuth2AuthorizationCodeGrantFilter.class);
        http.oauth2Login()
                .and()
                .logout()
                .addLogoutHandler(keycloakLogoutHandler)
                .logoutSuccessUrl("/");
//        http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        return http.build();

    }

    @Bean
    public AddFhirUserToSessionFilter addFhirUserToSessionFilter() {
        return new AddFhirUserToSessionFilter(patientFhirClientService, practitionerFhirClientService, relatedPersonFhirClientService);
    }
}
