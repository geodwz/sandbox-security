package io.github.geodwz.sandbox.security.project;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProjectApiClient {
	private final RestClient api;

	public ProjectApiClient(OAuth2AuthorizedClientManager clients, @Value("${project-api.base-url:http://localhost:8080}") String apiUrl) {
		this.api = RestClient.builder()
			.baseUrl(apiUrl)
			.defaultHeader(HttpHeaders.USER_AGENT, "m2m-client")
			.requestInterceptor(initialzeOAuth2ClientHttpRequestInterceptor(clients, "project-report-client"))
			.build();
	}

	private OAuth2ClientHttpRequestInterceptor initialzeOAuth2ClientHttpRequestInterceptor(
				OAuth2AuthorizedClientManager manager, String clientRegistrationId) {
		final OAuth2ClientHttpRequestInterceptor interceptor = new OAuth2ClientHttpRequestInterceptor(manager);
		interceptor.setClientRegistrationIdResolver(request -> clientRegistrationId);
		return interceptor;
	}

	public String listProjects() {
		return api.get().uri("/api/projects")
				.retrieve().body(String.class);
	}
}
