package nl.koppeltaal.poc.portal.controllers;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Controller
@RequestMapping("/api/codesystem")
public class CodeSystemProxyController {
	private static final Map<String, String> CODE_SYSTEMS = new HashMap<>();
	static {
		CODE_SYSTEMS.put("http://terminology.hl7.org/CodeSystem/v2-0131", "https://terminology.hl7.org/2.1.0/CodeSystem-v2-0131.json");
		CODE_SYSTEMS.put("http://terminology.hl7.org/CodeSystem/v3-RoleCode", "https://terminology.hl7.org/2.1.0/CodeSystem-v3-RoleCode.json");
	}

	private static final Map<String, ResponseEntity<String>> CODE_SYSTEMS_CACHE = new HashMap<>();

	@RequestMapping( method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> get(@RequestParam("codeSystem") String codeSystem) throws IOException {
		if (CODE_SYSTEMS.containsKey(codeSystem)) {
			if (CODE_SYSTEMS_CACHE.containsKey(codeSystem)) {
				return CODE_SYSTEMS_CACHE.get(codeSystem);
			} else {
				ResponseEntity<String> responseEntity = readCodeSystem(codeSystem);
				if (responseEntity.getStatusCode().is2xxSuccessful()) {
					CODE_SYSTEMS_CACHE.put(codeSystem, responseEntity);
				}
				return responseEntity;
			}
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	private ResponseEntity<String> readCodeSystem(String codeSystem) throws IOException {
		ResponseEntity<String> responseEntity;
		HttpGet httpGet = new HttpGet(CODE_SYSTEMS.get(codeSystem));
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			CloseableHttpResponse execute = client.execute(httpGet);
			HttpEntity entity = execute.getEntity();
			InputStream content = entity.getContent();
			String encoding;
			Header contentEncoding = entity.getContentEncoding();
			if (contentEncoding != null) {
				encoding = contentEncoding.getValue();
			} else {
				encoding = StandardCharsets.UTF_8.name();
			}
			String json = IOUtils.toString(content, encoding);
			HttpStatus status = HttpStatus.valueOf(execute.getStatusLine().getStatusCode());
			responseEntity = new ResponseEntity<>(json, status);

		}
		return responseEntity;
	}
}
