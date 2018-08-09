
package com.android.example.github.api;

import android.arch.core.executor.testing.InstantTaskExecutorRule;

import com.android.example.github.util.LiveDataCallAdapterFactory;
import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.Project;
import com.android.example.github.vo.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.android.example.github.util.LiveDataTestUtil.getValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(JUnit4.class)
public class TeamworkServiceTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private TeamworkService service;

    private MockWebServer mockWebServer;

    @Before
    public void createService() throws IOException {
        mockWebServer = new MockWebServer();
        service = new Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build()
                .create(TeamworkService.class);
    }

    @After
    public void stopService() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void getUser() throws IOException, InterruptedException {
        enqueueResponse("user-yigit.json");
        User yigit = getValue(service.getUser("yigit")).body;

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath(), is("/users/yigit"));

        assertThat(yigit, notNullValue());
        assertThat(yigit.avatarUrl, is("https://avatars3.githubusercontent.com/u/89202?v=3"));
        assertThat(yigit.company, is("Google"));
        assertThat(yigit.blog, is("birbit.com"));
    }

    @Test
    public void getRepos() throws IOException, InterruptedException {
        enqueueResponse("projects-yigit.json");
        List<Project> projects = getValue(service.getProjects("yigit")).body;

        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getPath(), is("/users/yigit/projects"));

        assertThat(projects.size(), is(2));

        Project project = projects.get(0);
        assertThat(project.name, is("Brazil"));

        Project.Company company = project.company;
        assertThat(company, notNullValue());
        assertThat(company.id, is("113332"));
        assertThat(company.name, is("Cat"));

        Project project2 = projects.get(1);
        assertThat(project2.name, is("Time Machine R&D"));
    }

    @Test
    public void getContributors() throws IOException, InterruptedException {
        enqueueResponse("contributors.json");
        List<Contributor> contributors = getValue(
                service.getContributors(1)).body;
        assertThat(contributors.size(), is(3));
        Contributor yigit = contributors.get(0);
        assertThat(yigit.getLogin(), is("yigit"));
        assertThat(yigit.getAvatarUrl(), is("https://avatars3.githubusercontent.com/u/89202?v=3"));
        assertThat(yigit.getContributions(), is(291));
        assertThat(contributors.get(1).getLogin(), is("guavabot"));
        assertThat(contributors.get(2).getLogin(), is("coltin"));
    }

    @Test
    public void search() throws IOException, InterruptedException {
        enqueueResponse("projects.json");
        ApiResponse<GetProjectsResponse> response = getValue(
                service.getProjects());

        assertThat(response, notNullValue());
        assertThat(response.body.getProjects().size(), is(30));
        assertThat(response.getNextPage(), is(2));
    }

    private void enqueueResponse(String fileName) throws IOException {
        enqueueResponse(fileName, Collections.emptyMap());
    }

    private void enqueueResponse(String fileName, Map<String, String> headers) throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("api-response/" + fileName);
        BufferedSource source = Okio.buffer(Okio.source(inputStream));
        MockResponse mockResponse = new MockResponse();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            mockResponse.addHeader(header.getKey(), header.getValue());
        }
        mockWebServer.enqueue(mockResponse
                .setBody(source.readString(StandardCharsets.UTF_8)));
    }
}
