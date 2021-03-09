package nl.koppeltaal.poc.kt20.services;

import com.auth0.jwk.JwkException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.koppeltaal.poc.fhir.service.*;
import nl.koppeltaal.poc.fhir.utils.ResourceUtils;
import nl.koppeltaal.poc.generic.TokenStorage;
import nl.koppeltaal.poc.kt20.KeyUtils;
import nl.koppeltaal.poc.kt20.configuration.Kt20ClientConfiguration;
import nl.koppeltaal.poc.kt20.configuration.Kt20ServerConfiguration;
import nl.koppeltaal.poc.kt20.valueobjects.LaunchData;
import nl.koppeltaal.poc.kt20.valueobjects.Task;
import nl.koppeltaal.poc.portal.controllers.SessionTokenStorage;
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
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 *
 */
@Service
public class Kt20LaunchService {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	Kt20ServerConfiguration kt20ServerConfiguration;

	@Autowired
	Kt20ClientConfiguration kt20ClientConfiguration;

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

	public LaunchData launchPatient(TokenStorage tokenStorage, Patient patient, String treatmentId, boolean isNew) throws GeneralSecurityException, IOException, JwkException {
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(tokenStorage, treatmentId);
		Assert.notNull(fhirDefinition, String.format("ActivityDefinition with id %s not found.", treatmentId));

		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getOrCreateTask(tokenStorage, patient, null, fhirDefinition, isNew);
		Assert.notNull(fhirTask, "FHIR Task not created");

		Task task = buildTask(fhirTask, new Reference(ResourceUtils.getReference(patient)));


		String launchToken = getLaunchToken(tokenStorage, task, fhirDefinition);
		return new LaunchData(getUrlForActivityDefinition(tokenStorage, fhirDefinition), launchToken, isRedirect(fhirDefinition));
	}

	public LaunchData launchPractitioner(TokenStorage tokenStorage, Practitioner practitioner, Patient patient, String treatmentId, boolean isNew) throws GeneralSecurityException, IOException, JwkException {
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(tokenStorage, treatmentId);
		Assert.notNull(fhirDefinition, String.format("ActivityDefinition with id %s not found.", treatmentId));
		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getOrCreateTask(tokenStorage, patient, practitioner, fhirDefinition, isNew);
		Assert.notNull(fhirTask, "FHIR Task not created");
		Task task = buildTask(fhirTask, new Reference(ResourceUtils.getReference(practitioner)));
		String launchToken = getLaunchToken(tokenStorage, task, fhirDefinition);
		return new LaunchData(getUrlForActivityDefinition(tokenStorage, fhirDefinition), launchToken, isRedirect(fhirDefinition));
	}

	public LaunchData launchRelatedPerson(TokenStorage tokenStorage, RelatedPerson fhirRelatedPerson, Patient fhirPatient, String treatmentId, boolean isNew) throws GeneralSecurityException, IOException, JwkException {
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(tokenStorage, treatmentId);
		Assert.notNull(fhirDefinition, String.format("ActivityDefinition with id %s not found.", treatmentId));
		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getOrCreateTask(tokenStorage, fhirPatient, null, fhirDefinition, isNew);
		Task task = buildTask(fhirTask, new Reference(ResourceUtils.getReference(fhirRelatedPerson)));
		Assert.notNull(fhirTask, "FHIR Task not created");
		String launchToken = getLaunchToken(tokenStorage, task, fhirDefinition);
		return new LaunchData(getUrlForActivityDefinition(tokenStorage, fhirDefinition), launchToken, isRedirect(fhirDefinition));
	}

	public LaunchData launchTaskPatient(SessionTokenStorage tokenStorage, Patient patient, String taskId) throws IOException, JwkException, GeneralSecurityException {
		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getResourceByReference(tokenStorage, "Task/" + taskId);
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(tokenStorage, fhirTask.getInstantiatesCanonical());
		Task task = buildTask(fhirTask, new Reference(ResourceUtils.getReference(patient)));
		String launchToken = getLaunchToken(tokenStorage, task, fhirDefinition);
		return new LaunchData(getUrlForActivityDefinition(tokenStorage, fhirDefinition), launchToken, isRedirect(fhirDefinition));
	}

	public LaunchData launchTaskPractitioner(SessionTokenStorage tokenStorage, Practitioner practitioner, String taskId) throws IOException, JwkException, GeneralSecurityException {
		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getResourceByReference(tokenStorage, "Task/" + taskId);
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(tokenStorage, fhirTask.getInstantiatesCanonical());
		Task task = buildTask(fhirTask, new Reference(ResourceUtils.getReference(practitioner)));
		String launchToken = getLaunchToken(tokenStorage, task, fhirDefinition);
		return new LaunchData(getUrlForActivityDefinition(tokenStorage, fhirDefinition), launchToken, isRedirect(fhirDefinition));
	}

	public LaunchData launchTaskRelatedPerson(SessionTokenStorage tokenStorage, RelatedPerson relatedPerson, String taskId) throws IOException, JwkException, GeneralSecurityException {
		org.hl7.fhir.r4.model.Task fhirTask = taskFhirClientService.getResourceByReference(tokenStorage, "Task/" + taskId);
		ActivityDefinition fhirDefinition = activityDefinitionFhirClientService.getResourceByReference(tokenStorage, fhirTask.getInstantiatesCanonical());
		Task task = buildTask(fhirTask, new Reference(ResourceUtils.getReference(relatedPerson)));
		String launchToken = getLaunchToken(tokenStorage, task, fhirDefinition);
		return new LaunchData(getUrlForActivityDefinition(tokenStorage, fhirDefinition), launchToken, isRedirect(fhirDefinition));
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

	private Task buildTask(org.hl7.fhir.r4.model.Task fhirTask, Reference forUser) {
		Task task = new Task();
		task.setResourceType("Task");
		task.setId(fhirTask.getIdElement().getIdPart());
		Assert.notNull(fhirTask.getInstantiatesCanonical(), "DefinitionReference in FHIR Task is null");
		task.getDefinitionReference().setReference(fhirTask.getInstantiatesCanonical());
		task.setOwner(buildUser(fhirTask.getOwner()));
		task.setForUser(buildUser(forUser));
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

	protected String getLaunchToken(TokenStorage tokenStorage, Task task, ActivityDefinition definition) throws GeneralSecurityException {
		try {
			JwtClaims claims = new JwtClaims();
			claims.setClaim("task", toMap(task));
			claims.setIssuedAt(NumericDate.now());
			claims.setAudience(getUrlForActivityDefinition(tokenStorage, definition));
			claims.setIssuer(kt20ServerConfiguration.getIssuer());
			claims.setExpirationTime(NumericDate.fromMilliseconds(System.currentTimeMillis() + 15 * 60 * 1000));
			claims.setJwtId(UUID.randomUUID().toString());

			JsonWebSignature jws = new JsonWebSignature();

			// The payload of the JWS is JSON content of the JWT Claims
			jws.setPayload(claims.toJson());

			KeyPair rsaKeyPair = KeyUtils.getRsaKeyPair(kt20ServerConfiguration.getPublicKey(), kt20ServerConfiguration.getPrivateKey());
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
			jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);

			final String payload = jws.getCompactSerialization();
			final boolean useJwe = isUseJwe(definition);
			return (useJwe ? buildJweWrapping(payload) : payload);
		} catch (JoseException | IOException | JwkException e) {
			throw new GeneralSecurityException(e);
		}

	}

	private String getUrlForActivityDefinition(TokenStorage tokenStorage, ActivityDefinition fhirDefinition) throws IOException, JwkException {
		Reference locationReference = fhirDefinition.getLocation();
		Location location = locationFhirClientService.getResourceByReference(tokenStorage, locationReference);
		for (Reference endpointReference : location.getEndpoint()) {
			Endpoint ep = endpointFhirClientService.getResourceByReference(tokenStorage, endpointReference);
			if (ep != null)
				return ep.getAddress();
		}
		return null;
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

	private Map toMap(Task task) {
		return objectMapper.convertValue(task, Map.class);
	}
}
