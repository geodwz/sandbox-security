package io.github.geodwz.sandbox.security.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ProjectClientController {
	private static final Logger log = LoggerFactory.getLogger(ProjectClientController.class);

	private final ProjectApiClient projects;
	ProjectClientController(ProjectApiClient projects) { this.projects = projects; }
	
	@GetMapping("/client/projects") 
	String projects() {
		log.info("Start client call server projects"); 
		return projects.listProjects(); 
	}

	@GetMapping("/api/hello")
	String hello() {
		log.debug("Sooo null");
		return "hello";
	}
}
