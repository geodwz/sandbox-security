package io.github.geodwz.sandbox.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class ProjectAuthorizationContractTests {

	private static final String PROJECT_JSON = "{\"name\":\"Security tutorial\",\"description\":\"Contract fixture\"}";

	@org.springframework.beans.factory.annotation.Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@BeforeEach
	void setUpMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@Test
	void AUTH_001_publicHealthIsAvailableWithoutAToken() throws Exception {
		mockMvc.perform(get("/api/public/health"))
				.andExpect(status().isOk());
	}

	@Test
	void AUTH_002_protectedProjectEndpointsRejectAnonymousRequests() throws Exception {
		mockMvc.perform(get("/api/projects"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void ERR_001_forbiddenResponsesUseTheSafeErrorContract() throws Exception {
		mockMvc.perform(delete("/api/projects/1").with(authority("ROLE_USER")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.error").value("forbidden"))
				.andExpect(jsonPath("$.message").value("Insufficient permissions"));
	}

	@Test
	void U2M_002_userCanReadProjects() throws Exception {
		mockMvc.perform(get("/api/projects").with(authority("ROLE_USER")))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/projects/1").with(authority("ROLE_USER")))
				.andExpect(status().isOk());
	}

	@Test
	void U2M_003_userCannotMutateProjectsOrAccessAdmin() throws Exception {
		assertForbidden(post("/api/projects"), "ROLE_USER");
		assertForbidden(put("/api/projects/1"), "ROLE_USER");
		assertForbidden(delete("/api/projects/1"), "ROLE_USER");
		assertForbidden(get("/api/admin/security"), "ROLE_USER");
	}

	@Test
	void U2M_004_editorCanCreateAndUpdateProjects() throws Exception {
		mockMvc.perform(json(post("/api/projects")).with(authority("ROLE_EDITOR")))
				.andExpect(status().isCreated());
		mockMvc.perform(json(put("/api/projects/1")).with(authority("ROLE_EDITOR")))
				.andExpect(status().isOk());
	}

	@Test
	void U2M_005_editorCannotDeleteOrAccessAdmin() throws Exception {
		assertForbidden(delete("/api/projects/1"), "ROLE_EDITOR");
		assertForbidden(get("/api/admin/users"), "ROLE_EDITOR");
	}

	@Test
	void U2M_006_adminCanDeleteProjectsAndAccessAdmin() throws Exception {
		mockMvc.perform(delete("/api/projects/1").with(authority("ROLE_ADMIN")))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/admin/security").with(authority("ROLE_ADMIN")))
				.andExpect(status().isOk());
	}

	@Test
	void M2M_001_and_M2M_002_readScopeCanReadButCannotMutate() throws Exception {
		mockMvc.perform(get("/api/projects").with(authority("SCOPE_project:read")))
				.andExpect(status().isOk());
		assertForbidden(post("/api/projects"), "SCOPE_project:read");
		assertForbidden(put("/api/projects/1"), "SCOPE_project:read");
		assertForbidden(delete("/api/projects/1"), "SCOPE_project:read");
	}

	@Test
	void M2M_003_writeScopeCanCreateAndUpdateButM2M_004CannotDelete() throws Exception {
		mockMvc.perform(json(post("/api/projects")).with(authority("SCOPE_project:write")))
				.andExpect(status().isCreated());
		mockMvc.perform(json(put("/api/projects/1")).with(authority("SCOPE_project:write")))
				.andExpect(status().isOk());
		assertForbidden(delete("/api/projects/1"), "SCOPE_project:write");
	}

	@Test
	void M2M_005_deleteScopeCanDeleteButM2M_006CannotUseHumanEndpoints() throws Exception {
		mockMvc.perform(delete("/api/projects/1").with(authority("SCOPE_project:delete")))
				.andExpect(status().isNoContent());
		assertForbidden(get("/api/me"), "SCOPE_project:delete");
		assertForbidden(get("/api/admin/security"), "SCOPE_project:delete");
	}

	@Test
	void NFR_001_corsAllowsOnlyTheKnownVueDevelopmentOrigin() throws Exception {
		mockMvc.perform(options("/api/projects")
				.header("Origin", "http://localhost:5173")
				.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isOk());
	}

	private void assertForbidden(MockHttpServletRequestBuilder request, String authority) throws Exception {
		mockMvc.perform(json(request).with(authority(authority)))
				.andExpect(status().isForbidden());
	}

	private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request) {
		return request.contentType(MediaType.APPLICATION_JSON).content(PROJECT_JSON);
	}

	private org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor authority(
			String authority) {
		return jwt().authorities(new SimpleGrantedAuthority(authority));
	}
}
