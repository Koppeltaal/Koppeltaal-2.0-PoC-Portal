package nl.koppeltaal.poc.portal.controllers;

import com.auth0.jwk.JwkException;
import nl.koppeltaal.poc.fhir.dto.BaseDto;
import nl.koppeltaal.poc.fhir.dto.DtoConverter;
import nl.koppeltaal.poc.fhir.exception.EnitityNotFoundException;
import nl.koppeltaal.poc.fhir.service.BaseFhirClientService;
import nl.koppeltaal.poc.utils.UrlUtils;
import org.hl7.fhir.r4.model.DomainResource;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class BaseResourceController<Dto extends BaseDto, Resource extends DomainResource> {

	final BaseFhirClientService<Dto, Resource> fhirClientService;
	final DtoConverter<Dto, Resource> dtoConverter;

	public BaseResourceController(BaseFhirClientService<Dto, Resource> fhirClientService, DtoConverter<Dto, Resource> dtoConverter) {
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
			throw new EnitityNotFoundException("Cannot locate activitydefinition " + reference);
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
	public Dto put(HttpSession httpSession, HttpServletRequest request, @RequestBody Dto dto) throws IOException, JwkException {
		return dtoConverter.convert(fhirClientService.storeResource(UrlUtils.getServerUrl("", request), dtoConverter.convert(dto)));
	}

}
