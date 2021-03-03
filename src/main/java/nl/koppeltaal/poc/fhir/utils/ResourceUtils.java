package nl.koppeltaal.poc.fhir.utils;

import ca.uhn.fhir.model.api.IResource;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.BaseResource;

/**
 *
 */
public class ResourceUtils {
	public static String getReference(IAnyResource resource) {
		IIdType idElement = resource.getIdElement();
		if (idElement != null) {
			return String.format("%s/%s", idElement.getResourceType(), idElement.getIdPart());
		}
		return null;
	}
}
