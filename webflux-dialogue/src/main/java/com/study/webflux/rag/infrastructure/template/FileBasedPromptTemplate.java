package com.study.webflux.rag.infrastructure.template;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class FileBasedPromptTemplate {

	public String load(String templateName) {
		return load(templateName, Map.of());
	}

	public String load(String templateName, Map<String, String> variables) {
		try {
			String path = "templates/" + templateName + ".txt";
			ClassPathResource resource = new ClassPathResource(path);
			String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

			String result = template;
			for (Map.Entry<String, String> entry : variables.entrySet()) {
				result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
			}
			return result;
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to load template: " + templateName, e);
		}
	}
}
