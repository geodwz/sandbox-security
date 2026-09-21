package io.github.geodwz.sandbox.security.project;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
	private final ProjectService service;
	public ProjectController(ProjectService service) { this.service = service; }
	@GetMapping public List<Project> all() { return service.findAll(); }
	@GetMapping("/{id}") public Project one(@PathVariable long id) { return service.findById(id); }
	@PostMapping @ResponseStatus(HttpStatus.CREATED) public Project create(@RequestBody ProjectRequest request) { return service.create(request); }
	@PutMapping("/{id}") public Project update(@PathVariable long id, @RequestBody ProjectRequest request) { return service.update(id, request); }
	@DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable long id) { service.delete(id); }
}
