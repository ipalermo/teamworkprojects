
package com.android.example.teamwork.repository;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import com.android.example.teamwork.api.ApiResponse;
import com.android.example.teamwork.api.GetProjectsResponse;
import com.android.example.teamwork.api.TeamworkService;
import com.android.example.teamwork.db.ProjectDao;
import com.android.example.teamwork.db.TeamworkDb;
import com.android.example.teamwork.util.AbsentLiveData;
import com.android.example.teamwork.util.InstantAppExecutors;
import com.android.example.teamwork.util.TestUtil;
import com.android.example.teamwork.vo.Contributor;
import com.android.example.teamwork.vo.GetProjectsResult;
import com.android.example.teamwork.vo.Project;
import com.android.example.teamwork.vo.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Response;

import static com.android.example.teamwork.util.ApiUtil.successCall;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@RunWith(JUnit4.class)
public class ProjectRepositoryTest {
    private ProjectRepository repository;
    private ProjectDao dao;
    private TeamworkService service;
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();
    @Before
    public void init() {
        dao = mock(ProjectDao.class);
        service = mock(TeamworkService.class);
        TeamworkDb db = mock(TeamworkDb.class);
        when(db.projectDao()).thenReturn(dao);
        repository = new ProjectRepository(new InstantAppExecutors(), db, dao, service);
    }

    @Test
    public void loadRepoFromNetwork() throws IOException {
        MutableLiveData<Project> dbData = new MutableLiveData<>();
        when(dao.load(10)).thenReturn(dbData);

        Project project = TestUtil.createProject(10, "bar", "desc");
        LiveData<ApiResponse<Project>> call = successCall(project);
        when(service.getProject(10)).thenReturn(call);

        LiveData<Resource<Project>> data = repository.loadProject(10);
        verify(dao).load(10);
        verifyNoMoreInteractions(service);

        Observer observer = mock(Observer.class);
        data.observeForever(observer);
        verifyNoMoreInteractions(service);
        verify(observer).onChanged(Resource.loading(null));
        MutableLiveData<Project> updatedDbData = new MutableLiveData<>();
        when(dao.load(10)).thenReturn(updatedDbData);

        dbData.postValue(null);
        verify(service).getProject(10);
        verify(dao).insert(project);

        updatedDbData.postValue(project);
        verify(observer).onChanged(Resource.success(project));
    }

    @Test
    public void loadContributors() throws IOException {
        MutableLiveData<List<Contributor>> dbData = new MutableLiveData<>();
        when(dao.loadContributors(10)).thenReturn(dbData);

        LiveData<Resource<List<Contributor>>> data = repository.loadContributors(
                10);
        verify(dao).loadContributors(10);

        verify(service, never()).getContributors(anyInt());

        Project project = TestUtil.createProject(10, "bar", "desc");
        Contributor contributor = TestUtil.createContributor(project, "log", 3);
        // network does not send these
        contributor.setProjectId(10);
        List<Contributor> contributors = Collections.singletonList(contributor);
        LiveData<ApiResponse<List<Contributor>>> call = successCall(contributors);
        when(service.getContributors(10))
                .thenReturn(call);

        Observer<Resource<List<Contributor>>> observer = mock(Observer.class);
        data.observeForever(observer);

        verify(observer).onChanged(Resource.loading( null));

        MutableLiveData<List<Contributor>> updatedDbData = new MutableLiveData<>();
        when(dao.loadContributors(10)).thenReturn(updatedDbData);
        dbData.setValue(Collections.emptyList());

        verify(service).getContributors(10);
        ArgumentCaptor<List<Contributor>> inserted = ArgumentCaptor.forClass((Class) List.class);
        verify(dao).insertContributors(inserted.capture());


        assertThat(inserted.getValue().size(), is(1));
        Contributor first = inserted.getValue().get(0);
        assertThat(first.getProjectId(), is(10));

        updatedDbData.setValue(contributors);
        verify(observer).onChanged(Resource.success(contributors));
    }

    @Test
    public void searchNextPage_null() {
        when(dao.findSearchResult()).thenReturn(null);
        Observer<Resource<Boolean>> observer = mock(Observer.class);
        repository.projectsNextPage().observeForever(observer);
        verify(observer).onChanged(null);
    }

    @Test
    public void search_fromDb() {
        List<Integer> ids = Arrays.asList(1, 2);

        Observer<Resource<List<Project>>> observer = mock(Observer.class);
        MutableLiveData<GetProjectsResult> dbSearchResult = new MutableLiveData<>();
        MutableLiveData<List<Project>> repositories = new MutableLiveData<>();

        when(dao.search()).thenReturn(dbSearchResult);

        repository.getProjects().observeForever(observer);

        verify(observer).onChanged(Resource.loading(null));
        verifyNoMoreInteractions(service);
        reset(observer);

        GetProjectsResult dbResult = new GetProjectsResult(ids, null);
        when(dao.loadOrdered(ids)).thenReturn(repositories);

        dbSearchResult.postValue(dbResult);

        List<Project> projectList = new ArrayList<>();
        repositories.postValue(projectList);
        verify(observer).onChanged(Resource.success(projectList));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void search_fromServer() {
        List<Integer> ids = Arrays.asList(1, 2);
        Project project1 = TestUtil.createProject(1, "repo 1", "desc 1");
        Project project2 = TestUtil.createProject(2, "repo 2", "desc 2");

        Observer<Resource<List<Project>>> observer = mock(Observer.class);
        MutableLiveData<GetProjectsResult> dbSearchResult = new MutableLiveData<>();
        MutableLiveData<List<Project>> repositories = new MutableLiveData<>();

        GetProjectsResponse apiResponse = new GetProjectsResponse();
        List<Project> projectList = Arrays.asList(project1, project2);
        apiResponse.setProjects(projectList);

        MutableLiveData<ApiResponse<GetProjectsResponse>> callLiveData = new MutableLiveData<>();
        when(service.getProjects()).thenReturn(callLiveData);

        when(dao.search()).thenReturn(dbSearchResult);

        repository.getProjects().observeForever(observer);

        verify(observer).onChanged(Resource.loading(null));
        verifyNoMoreInteractions(service);
        reset(observer);

        when(dao.loadOrdered(ids)).thenReturn(repositories);
        dbSearchResult.postValue(null);
        verify(dao, never()).loadOrdered(anyObject());

        verify(service).getProjects();
        MutableLiveData<GetProjectsResult> updatedResult = new MutableLiveData<>();
        when(dao.search()).thenReturn(updatedResult);
        updatedResult.postValue(new GetProjectsResult(ids, null));

        callLiveData.postValue(new ApiResponse<>(Response.success(apiResponse)));
        verify(dao).insertRepos(projectList);
        repositories.postValue(projectList);
        verify(observer).onChanged(Resource.success(projectList));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void search_fromServer_error() {
        when(dao.search()).thenReturn(AbsentLiveData.create());
        MutableLiveData<ApiResponse<GetProjectsResponse>> apiResponse = new MutableLiveData<>();
        when(service.getProjects()).thenReturn(apiResponse);

        Observer<Resource<List<Project>>> observer = mock(Observer.class);
        repository.getProjects().observeForever(observer);
        verify(observer).onChanged(Resource.loading(null));

        apiResponse.postValue(new ApiResponse<>(new Exception("idk")));
        verify(observer).onChanged(Resource.error("idk", null));
    }
}