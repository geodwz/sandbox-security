package io.github.geodwz.sandbox.security.projet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.geodwz.sandbox.security.project.ProjectApiClient;

@SpringBootTest 
public class ProjectApiClientTest {

    @Autowired 
    ProjectApiClient projectApiClient;

    @Test 
    void call_listprject() {
        var result = projectApiClient.listProjects();
        System.out.println(result);
    }
    
}
