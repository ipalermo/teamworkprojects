
package com.android.example.teamwork.util;

import com.android.example.teamwork.vo.Contributor;
import com.android.example.teamwork.vo.Project;
import com.android.example.teamwork.vo.User;

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
            projects.add(createProject( i, name + i, description + i));
        }
        return projects;
    }

    public static Project createProject(String name, String description) {
        return createProject(Project.UNKNOWN_ID, name, description);
    }

    public static Project createProject(int id, String name, String description) {
        return new Project(id, name,
                description, new Project.Company("company" + String.valueOf(id) , "name"), true);
    }

    public static Contributor createContributor(Project project, String login, int contributions) {
        Contributor contributor = new Contributor(login, contributions, null);
        contributor.setProjectId(project.id);
        return contributor;
    }
}
