
package com.android.example.github.repository;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;

import com.android.example.github.api.GetProjectsResponse;
import com.android.example.github.api.TeamworkService;
import com.android.example.github.db.ProjectDao;
import com.android.example.github.db.TeamworkDb;
import com.android.example.github.util.TestUtil;
import com.android.example.github.vo.GetProjectsResult;
import com.android.example.github.vo.Project;
import com.android.example.github.vo.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class ProjectsNextPageTaskTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private TeamworkService service;

    private TeamworkDb db;

    private ProjectDao projectDao;

    private ProjectsNextPageTask task;

    private LiveData<Resource<Boolean>> value;

    private Observer<Resource<Boolean>> observer;

    @Before
    public void init() {
        service = mock(TeamworkService.class);
        db = mock(TeamworkDb.class);
        projectDao = mock(ProjectDao.class);
        when(db.projectDao()).thenReturn(projectDao);
        task = new ProjectsNextPageTask(service, db);
        //noinspection unchecked
        observer = mock(Observer.class);
        task.getLiveData().observeForever(observer);
    }

    @Test
    public void withoutResult() {
        when(projectDao.search()).thenReturn(null);
        task.run();
        verify(observer).onChanged(null);
        verifyNoMoreInteractions(observer);
        verifyNoMoreInteractions(service);
    }

    @Test
    public void noNextPage() {
        createDbResult(null);
        task.run();
        verify(observer).onChanged(Resource.success(false));
        verifyNoMoreInteractions(observer);
        verifyNoMoreInteractions(service);
    }

    @Test
    public void nextPageWithNull() throws IOException {
        createDbResult(1);
        GetProjectsResponse result = new GetProjectsResponse();
        List<Project> projects = TestUtil.createProjects(10, "a", "b", "c");
        result.setProjects(projects);
        Call<GetProjectsResponse> call = createCall(result, null);
        when(service.getProjects(1)).thenReturn(call);
        task.run();
        verify(projectDao).insertRepos(projects);
        verify(observer).onChanged(Resource.success(false));
    }

    @Test
    public void nextPageWithMore() throws IOException {
        createDbResult(1);
        GetProjectsResponse result = new GetProjectsResponse();
        List<Project> projects = TestUtil.createProjects(10, "a", "b", "c");
        result.setProjects(projects);
        result.setNextPage(2);
        Call<GetProjectsResponse> call = createCall(result, 2);
        when(service.getProjects(1)).thenReturn(call);
        task.run();
        verify(projectDao).insertRepos(projects);
        verify(observer).onChanged(Resource.success(true));
    }

    @Test
    public void nextPageApiError() throws IOException {
        createDbResult(1);
        Call<GetProjectsResponse> call = mock(Call.class);
        when(call.execute()).thenReturn(Response.error(400, ResponseBody.create(
                MediaType.parse("txt"), "bar")));
        when(service.getProjects(1)).thenReturn(call);
        task.run();
        verify(observer).onChanged(Resource.error("bar", true));
    }

    @Test
    public void nextPageIOError() throws IOException {
        createDbResult(1);
        Call<GetProjectsResponse> call = mock(Call.class);
        when(call.execute()).thenThrow(new IOException("bar"));
        when(service.getProjects(1)).thenReturn(call);
        task.run();
        verify(observer).onChanged(Resource.error("bar", true));
    }

    private void createDbResult(Integer nextPage) {
        GetProjectsResult result = new GetProjectsResult(Collections.emptyList(),
                nextPage);
        when(projectDao.findSearchResult()).thenReturn(result);
    }

    private Call<GetProjectsResponse> createCall(GetProjectsResponse body, Integer nextPage)
            throws IOException {
        Headers headers = nextPage == null ? null : Headers
                .of("link",
                        "<https://api.github.com/getProjects/repositories?q=foo&page=" + nextPage
                                + ">; rel=\"next\"");
        Response<GetProjectsResponse> success = headers == null ?
                Response.success(body) : Response.success(body, headers);
        Call call = mock(Call.class);
        when(call.execute()).thenReturn(success);
        //noinspection unchecked
        return call;
    }
}