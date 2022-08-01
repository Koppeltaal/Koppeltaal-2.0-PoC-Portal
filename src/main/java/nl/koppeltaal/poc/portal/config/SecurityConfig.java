package nl.koppeltaal.poc.portal.config;

import nl.koppeltaal.poc.portal.filter.AddFhirUserToSessionFilter;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PatientFhirClientService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.PractitionerFhirClientService;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.RelatedPersonFhirClientService;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakSecurityContextRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@KeycloakConfiguration
class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    final PatientFhirClientService patientFhirClientService;
    final PractitionerFhirClientService practitionerFhirClientService;
    final RelatedPersonFhirClientService relatedPersonFhirClientService;

    SecurityConfig(PatientFhirClientService patientFhirClientService, PractitionerFhirClientService practitionerFhirClientService, RelatedPersonFhirClientService relatedPersonFhirClientService) {
        this.patientFhirClientService = patientFhirClientService;
        this.practitionerFhirClientService = practitionerFhirClientService;
        this.relatedPersonFhirClientService = relatedPersonFhirClientService;
    }


    // Submits the KeycloakAuthenticationProvider to the AuthenticationManager
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    // Specifies the session authentication strategy
    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http
                .csrf()
                    .disable()
                .authorizeRequests()
                .anyRequest()
                    .hasAnyRole("patient", "practitioner", "relatedPerson")
                .and()
                    .addFilterAfter(addFhirUserToSessionFilter(), KeycloakSecurityContextRequestFilter.class);
    }

    @Bean
    public AddFhirUserToSessionFilter addFhirUserToSessionFilter() {
        return new AddFhirUserToSessionFilter(patientFhirClientService, practitionerFhirClientService, relatedPersonFhirClientService);
    }
}
