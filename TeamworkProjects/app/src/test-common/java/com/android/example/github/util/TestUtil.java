
package com.android.example.github.util;

import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.Project;
import com.android.example.github.vo.User;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    public static User createUser(String login) {
        return new User(login, null,
                login + " name", null, null, null);
    }

    public static List<Project> createProjects(int count, String owner, String name,
                                               String description) {
        List<Project> projects = new ArrayList<>();
        for(int i = 0; i < count; i ++) {
            projects.add(createProject(owner + i, name + i, description + i));
        }
        return projects;
    }

    public static Project createProject(String owner, String name, String description) {
        return createProject(Project.UNKNOWN_ID, owner, name, description);
    }

    public static Project createProject(int id, String owner, String name, String description) {
        return new Project(id, name,
                description, new Project.Company(owner, null), true);
    }

    public static Contributor createContributor(Project project, String login, int contributions) {
        Contributor contributor = new Contributor(login, contributions, null);
        contributor.setProjectId(project.name);
        contributor.setRepoOwner(project.company.id);
        return contributor;
    }
}
