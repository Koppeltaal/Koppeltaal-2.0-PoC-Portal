package nl.koppeltaal.poc.kt20.services;

import com.auth0.jwk.JwkException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.koppeltaal.poc.kt20.KeyUtils;
import nl.koppeltaal.poc.kt20.configuration.Kt20ClientConfiguration;
import nl.koppeltaal.poc.kt20.valueobjects.LaunchData;
import nl.koppeltaal.poc.kt20.valueobjects.Task;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.*;
import nl.koppeltaal.springbootstarterjwks.config.JwksConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.Use;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static nl.koppeltaal.spring.boot.starter.smartservice.constants.FhirConstant.KT2_EXTENSION__ENDPOINT;
import static nl.koppeltaal.spring.boot.starter.smartservice.utils.ResourceUtils.getReference;

/**
 *
 */
@Service
public class Kt20LaunchService {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	Kt20ClientConfiguration kt20ClientConfiguration;

	@Autowired
	JwksConfiguration jwksConfiguration;

	@Value("${kt20.server.issuer:koppeltaal-2.0-poc-portal}")
	String issuer;

	@Autowired
	PatientFhirClientService patientFhirClientService;
	@Autowired
	PractitionerFhirClientService practitionerFhirClientService;
	@Autowired
	ActivityDefinitionFhirClientService activityDefinitionFhirClientService;
	@Autowired
	TaskFhirClientService taskFhirClientService;

	@Autowired
	EndpointFhirClientService endpointFhirClientService;

	@Autowired
	LocationFhirClientService locationFhirClientService;

	public static Extension findExtensionByUrl(ActivityDefinition activityDefinition, String url) {
		final List<Extension> extensions = activityDefinition.getExtension();
		if (extensions == null) {
			return null;
		}
		Extension found = null;
		for (Extension extension : extensions) {
			if (StringUtils.equals(extension.getUrl(), url)) {
				found = extension;
				break;
			}
		}
		return found;
	}

	public LaunchData launchPatient(Patient patient, String treatmentId, boolean isNew) throws GeneralSecurityException, IOException, JwkException {
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(treatmentId);
		Assert.notNull(fhirDefinition, String.format("ActivityDefinition with id %s not found.", treatmentId));

		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getOrCreateTask(patient, null, fhirDefinition, isNew);
		Assert.notNull(fhirTask, "FHIR Task not created");

		Task task = buildTask(fhirTask);

		String launchToken = getLaunchToken(task, fhirDefinition, getReference(patient));
		return new LaunchData(getUrlForActivityDefinition(fhirDefinition), launchToken, isRedirect(fhirDefinition));
	}

	public LaunchData launchPractitioner(Practitioner practitioner, Patient patient, String treatmentId, boolean isNew) throws GeneralSecurityException, IOException, JwkException {
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(treatmentId);
		Assert.notNull(fhirDefinition, String.format("ActivityDefinition with id %s not found.", treatmentId));
		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getOrCreateTask(patient, practitioner, fhirDefinition, isNew);
		Assert.notNull(fhirTask, "FHIR Task not created");
		Task task = buildTask(fhirTask);
		String launchToken = getLaunchToken(task, fhirDefinition, getReference(practitioner));
		return new LaunchData(getUrlForActivityDefinition(fhirDefinition), launchToken, isRedirect(fhirDefinition));
	}

	public LaunchData launchRelatedPerson(RelatedPerson fhirRelatedPerson, Patient fhirPatient, String treatmentId, boolean isNew) throws GeneralSecurityException, IOException, JwkException {
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(treatmentId);
		Assert.notNull(fhirDefinition, String.format("ActivityDefinition with id %s not found.", treatmentId));
		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getOrCreateTask(fhirPatient, null, fhirDefinition, isNew);
		Task task = buildTask(fhirTask);
		Assert.notNull(fhirTask, "FHIR Task not created");
		String launchToken = getLaunchToken(task, fhirDefinition, getReference(fhirRelatedPerson));
		return new LaunchData(getUrlForActivityDefinition(fhirDefinition), launchToken, isRedirect(fhirDefinition));
	}

	public LaunchData launchTaskPatient(Patient patient, String taskId) throws IOException, JwkException, GeneralSecurityException {
		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getResourceByReference("Task/" + taskId);
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(fhirTask.getInstantiatesCanonical());
		Task task = buildTask(fhirTask);
		String launchToken = getLaunchToken(task, fhirDefinition, getReference(patient));
		return new LaunchData(getUrlForActivityDefinition(fhirDefinition), launchToken, isRedirect(fhirDefinition));
	}

	public LaunchData launchTaskPractitioner(Practitioner practitioner, String taskId) throws IOException, JwkException, GeneralSecurityException {
		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getResourceByReference("Task/" + taskId);
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(fhirTask.getInstantiatesCanonical());
		Task task = buildTask(fhirTask);
		String launchToken = getLaunchToken(task, fhirDefinition, getReference(practitioner));
		return new LaunchData(getUrlForActivityDefinition(fhirDefinition), launchToken, isRedirect(fhirDefinition));
	}

	public LaunchData launchTaskRelatedPerson(RelatedPerson relatedPerson, String taskId) throws IOException, JwkException, GeneralSecurityException {
		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getResourceByReference("Task/" + taskId);
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(fhirTask.getInstantiatesCanonical());
		Task task = buildTask(fhirTask);
		String launchToken = getLaunchToken(task, fhirDefinition, getReference(relatedPerson));
		return new LaunchData(getUrlForActivityDefinition(fhirDefinition), launchToken, isRedirect(fhirDefinition));
	}

	private Task.Identifier buildIdentifier(String id) {
		Task.Identifier identifier = new Task.Identifier();
		identifier.setValue(id);
		return identifier;
	}

	private List<Task.Identifier> buildIdentifier(List<Identifier> fhirTaskIdentifier) {
		if (fhirTaskIdentifier.isEmpty()) {
			return null;
		}
		List<Task.Identifier> rv = new ArrayList<>();
		for (Identifier identifier : fhirTaskIdentifier) {
			rv.add(buildIdentifier(identifier.getValue()));
		}
		return rv;
	}

	private String buildJweWrapping(String payload) throws NoSuchAlgorithmException, InvalidKeySpecException, JoseException {
		JsonWebEncryption jwe = new JsonWebEncryption();
		jwe.setAlgorithmHeaderValue("RSA-OAEP");
		jwe.setEncryptionMethodHeaderParameter("A128CBC-HS256");
		jwe.setKey(KeyUtils.getRsaPublicKey(kt20ClientConfiguration.getPublicKey()));
		jwe.setContentTypeHeaderValue("JWT");
		jwe.setPayload(payload);
		return jwe.getCompactSerialization();
	}

	private Task buildTask(org.hl7.fhir.r4.model.Task fhirTask) {
		Task task = new Task();
		task.setResourceType("Task");
		task.setId(fhirTask.getIdElement().getIdPart());
		final String instantiatesCanonical = fhirTask.getInstantiatesCanonical();
		Assert.notNull(instantiatesCanonical, "Task.instantiatesCanonical is null");
		task.setInstantiatesCanonical(instantiatesCanonical);
		task.setOwner(buildUser(fhirTask.getOwner()));
		task.setRequester(buildUser(fhirTask.getRequester()));
		task.setIdentifier(buildIdentifier(fhirTask.getIdentifier()));
		return task;
	}

	private Task.User buildUser(Reference reference) {
		if (reference == null || reference.getReference() == null) return null;
		Task.User user = new Task.User();
		String ref = reference.getReference();
		user.setReference(ref);
		return user;
	}

	protected String getLaunchToken(Task task, ActivityDefinition definition, String launchingUserReference) throws GeneralSecurityException {
		try {
			JwtClaims claims = new JwtClaims();
			claims.setClaim("task", toMap(task));
			claims.setSubject(launchingUserReference);
			claims.setIssuedAt(NumericDate.now());
			claims.setAudience(getUrlForActivityDefinition(definition));
			claims.setIssuer(issuer);
			claims.setExpirationTime(NumericDate.fromMilliseconds(System.currentTimeMillis() + 15 * 60 * 1000));
			claims.setJwtId(UUID.randomUUID().toString());

			JsonWebSignature jws = new JsonWebSignature();

			// The payload of the JWS is JSON content of the JWT Claims
			jws.setPayload(claims.toJson());

			KeyPair rsaKeyPair = KeyUtils.getRsaKeyPair(jwksConfiguration.getSigningPublicKey(), jwksConfiguration.getSigningPrivateKey());
			PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(rsaKeyPair.getPublic());
			jwk.setPrivateKey(rsaKeyPair.getPrivate());
			jwk.setUse(Use.SIGNATURE);

			// The JWT is signed using the private key
			jws.setKey(jwk.getPrivateKey());

			// Set the Key ID (kid) header because it's just the polite thing to do.
			// We only have one key in this example but a using a Key ID helps
			// facilitate a smooth key rollover process
			jws.setKeyIdHeaderValue(KeyUtils.getFingerPrint(rsaKeyPair.getPublic()));

			// Set the signature algorithm on the JWT/JWS that will integrity protect the claims
			final String signingAlgorithm = jwksConfiguration.getSigningAlgorithm();
			jws.setAlgorithmHeaderValue(StringUtils.isNotBlank(signingAlgorithm) ? signingAlgorithm : AlgorithmIdentifiers.RSA_USING_SHA512);

			final String payload = jws.getCompactSerialization();
			final boolean useJwe = isUseJwe(definition);
			return useJwe ? buildJweWrapping(payload) : payload;
		} catch (JoseException | IOException | JwkException e) {
			throw new GeneralSecurityException(e);
		}

	}

	private String getUrlForActivityDefinition(ActivityDefinition fhirDefinition) throws IOException, JwkException {

		final List<Extension> endpointExtension = fhirDefinition.getExtensionsByUrl(KT2_EXTENSION__ENDPOINT);

		if (endpointExtension.isEmpty()) {
			throw new IllegalStateException("No endpoint found");
		}

		final Reference endpointReference = (Reference) endpointExtension.get(0).getValue();
		Endpoint endpoint = endpointFhirClientService.getResourceByReference(endpointReference);

		if (endpoint == null) {
			throw new IllegalStateException("No endpoint object found for reference " + endpointReference.getReference());
		}

		return endpoint.getAddress();
	}

	private boolean isRedirect(ActivityDefinition definition) {
		final Extension extension = findExtensionByUrl(definition, "http://gidsopenstandaarden.nl/kt2.0/launch_type");
		if (extension != null) {
			return StringUtils.equalsIgnoreCase(extension.getValue().castToString(extension.getValue()).getValue(), "redirect");
		}
		return false;
	}

	private boolean isUseJwe(ActivityDefinition definition) {
		final Extension extension = findExtensionByUrl(definition, "http://gidsopenstandaarden.nl/kt2.0/message_type");
		if (extension != null) {
			return StringUtils.equalsIgnoreCase(extension.getValue().castToString(extension.getValue()).getValue(), "jwe");
		}
		return kt20ClientConfiguration.isUseJwe();
	}

	@SuppressWarnings("unchecked")
	private Map<Object, Object> toMap(Task task) {
		return objectMapper.convertValue(task, Map.class);
	}
}
