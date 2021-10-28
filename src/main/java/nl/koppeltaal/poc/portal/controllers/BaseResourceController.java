package nl.koppeltaal.poc.portal.controllers;

import com.auth0.jwk.JwkException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.BaseDto;
import nl.koppeltaal.spring.boot.starter.smartservice.dto.DtoConverter;
import nl.koppeltaal.spring.boot.starter.smartservice.exception.EnitityNotFoundException;
import nl.koppeltaal.spring.boot.starter.smartservice.service.fhir.BaseFhirClientCrudService;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 */
public class BaseResourceController<Dto extends BaseDto, Resource extends DomainResource> {

	final BaseFhirClientCrudService<Dto, Resource> fhirClientService;
	final DtoConverter<Dto, Resource> dtoConverter;

	public BaseResourceController(BaseFhirClientCrudService<Dto, Resource> fhirClientService, DtoConverter<Dto, Resource> dtoConverter) {
		this.fhirClientService = fhirClientService;
		this.dtoConverter = dtoConverter;
	}

	@RequestMapping(value = "{reference}", method = RequestMethod.DELETE)
	public void delete(HttpSession httpSession, @PathVariable String reference) throws IOException, JwkException {
		fhirClientService.deleteResourceByReference(reference);
	}

	@RequestMapping(value = "{reference}", method = RequestMethod.GET)
	public Dto get(HttpSession httpSession, @PathVariable String reference) throws IOException, JwkException {
		Resource resource = fhirClientService.getResourceByReference(reference);
		if (resource != null) {
			return dtoConverter.convert(resource);
		} else {
			throw new EnitityNotFoundException("Cannot locate resource " + reference);
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<Dto> list(HttpSession httpSession) throws IOException, JwkException {
		List<Dto> rv = new ArrayList<>();
		List<Resource> resources = fhirClientService.getResources();
		for (Resource resource : resources) {
			rv.add(dtoConverter.convert(resource));
		}
		return rv;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public Dto put(HttpSession httpSession, HttpServletRequest request, @RequestBody Dto dto) throws IOException {
		return dtoConverter.convert(fhirClientService.storeResource(dtoConverter.convert(dto)));
	}

	String getReference(DomainResource resource) {
		IdType id = resource.getIdElement();
		return id.getResourceType()  + "/" + id.toUnqualifiedVersionless().getIdPart();
	}

}
