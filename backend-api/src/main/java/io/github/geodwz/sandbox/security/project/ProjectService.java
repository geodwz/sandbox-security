package io.github.geodwz.sandbox.security.project;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
	private final AtomicLong ids = new AtomicLong(1);
	private final List<Project> projects = new ArrayList<>(List.of(new Project(1, "Security tutorial", "In-memory example")));

	@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_EDITOR', 'ROLE_ADMIN', 'SCOPE_project:read')")
	public List<Project> findAll() { return List.copyOf(projects); }
	@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_EDITOR', 'ROLE_ADMIN', 'SCOPE_project:read')")
	public Project findById(long id) { return projects.stream().filter(project -> project.id() == id).findFirst().orElseThrow(); }
	@PreAuthorize("hasAnyAuthority('ROLE_EDITOR', 'ROLE_ADMIN', 'SCOPE_project:write')")
	public Project create(ProjectRequest request) { var project = new Project(ids.incrementAndGet(), request.name(), request.description()); projects.add(project); return project; }
	@PreAuthorize("hasAnyAuthority('ROLE_EDITOR', 'ROLE_ADMIN', 'SCOPE_project:write')")
	public Project update(long id, ProjectRequest request) { var project = new Project(id, request.name(), request.description()); projects.removeIf(value -> value.id() == id); projects.add(project); return project; }
	@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'SCOPE_project:delete')")
	public void delete(long id) { projects.removeIf(project -> project.id() == id); }
}
