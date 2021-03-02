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
public class BaseResourceController<D extends BaseDto, R extends DomainResource> {

	final BaseFhirClientService<D, R> fhirClientService;
	final DtoConverter<D, R> dtoConverter;

	public BaseResourceController(BaseFhirClientService fhirClientService, DtoConverter dtoConverter) {
		this.fhirClientService = fhirClientService;
		this.dtoConverter = dtoConverter;
	}

	@RequestMapping(value = "{reference}", method = RequestMethod.DELETE)
	public void delete(HttpSession httpSession, @PathVariable String reference) throws IOException, JwkException {
		fhirClientService.deleteResourceByReference(new SessionTokenStorage(httpSession), reference);
	}

	@RequestMapping(value = "{reference}", method = RequestMethod.GET)
	public D get(HttpSession httpSession, @PathVariable String reference) throws IOException, JwkException {
		R activitydefinition = fhirClientService.getResourceByReference(new SessionTokenStorage(httpSession), reference);
		if (activitydefinition != null) {
			return dtoConverter.convert(activitydefinition);
		} else {
			throw new EnitityNotFoundException("Cannot locate activitydefinition " + reference);
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	public List<D> list(HttpSession httpSession) throws IOException, JwkException {
		List<D> rv = new ArrayList<>();
		List<R> activitydefinitions = fhirClientService.getResources(new SessionTokenStorage(httpSession));
		for (R activitydefinition : activitydefinitions) {
			rv.add(dtoConverter.convert(activitydefinition));
		}
		return rv;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public D put(HttpSession httpSession, HttpServletRequest request, @RequestBody D activitydefinitionDto) throws IOException, JwkException {
		return dtoConverter.convert(fhirClientService.storeResource(new SessionTokenStorage(httpSession), UrlUtils.getServerUrl("", request), dtoConverter.convert(activitydefinitionDto)));
	}

}
