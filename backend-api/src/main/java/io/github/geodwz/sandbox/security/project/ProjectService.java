package io.github.geodwz.sandbox.security.project;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
	private static final Logger log = LoggerFactory.getLogger(ProjectService.class);


	private final AtomicLong ids = new AtomicLong(1);
	private final List<Project> projects = new ArrayList<>(List.of(new Project(1, "Security tutorial", "In-memory example")));

	public List<Project> findAll() { 
		log.info("Start Find all");
		return List.copyOf(projects); 
	}
	
	public Project findById(long id) { return projects.stream().filter(project -> project.id() == id).findFirst().orElseThrow(); }
	
	public Project create(ProjectRequest request) { var project = new Project(ids.incrementAndGet(), request.name(), request.description()); projects.add(project); return project; }
	
	public Project update(long id, ProjectRequest request) { var project = new Project(id, request.name(), request.description()); projects.removeIf(value -> value.id() == id); projects.add(project); return project; }
	
	public void delete(long id) { projects.removeIf(project -> project.id() == id); }
}
