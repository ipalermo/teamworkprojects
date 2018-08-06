
package com.android.example.github.repository;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import com.android.example.github.api.ApiResponse;
import com.android.example.github.api.GetProjectsResponse;
import com.android.example.github.api.GithubService;
import com.android.example.github.db.GithubDb;
import com.android.example.github.db.RepoDao;
import com.android.example.github.util.AbsentLiveData;
import com.android.example.github.util.InstantAppExecutors;
import com.android.example.github.util.TestUtil;
import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.GetProjectsResult;
import com.android.example.github.vo.Repo;
import com.android.example.github.vo.Resource;

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

import static com.android.example.github.util.ApiUtil.successCall;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@RunWith(JUnit4.class)
public class RepoRepositoryTest {
    private RepoRepository repository;
    private RepoDao dao;
    private GithubService service;
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();
    @Before
    public void init() {
        dao = mock(RepoDao.class);
        service = mock(GithubService.class);
        GithubDb db = mock(GithubDb.class);
        when(db.repoDao()).thenReturn(dao);
        repository = new RepoRepository(new InstantAppExecutors(), db, dao, service);
    }

    @Test
    public void loadRepoFromNetwork() throws IOException {
        MutableLiveData<Repo> dbData = new MutableLiveData<>();
        when(dao.load("foo", "bar")).thenReturn(dbData);

        Repo repo = TestUtil.createRepo("foo", "bar", "desc");
        LiveData<ApiResponse<Repo>> call = successCall(repo);
        when(service.getRepo("foo", "bar")).thenReturn(call);

        LiveData<Resource<Repo>> data = repository.loadRepo("foo", "bar");
        verify(dao).load("foo", "bar");
        verifyNoMoreInteractions(service);

        Observer observer = mock(Observer.class);
        data.observeForever(observer);
        verifyNoMoreInteractions(service);
        verify(observer).onChanged(Resource.loading(null));
        MutableLiveData<Repo> updatedDbData = new MutableLiveData<>();
        when(dao.load("foo", "bar")).thenReturn(updatedDbData);

        dbData.postValue(null);
        verify(service).getRepo("foo", "bar");
        verify(dao).insert(repo);

        updatedDbData.postValue(repo);
        verify(observer).onChanged(Resource.success(repo));
    }

    @Test
    public void loadContributors() throws IOException {
        MutableLiveData<List<Contributor>> dbData = new MutableLiveData<>();
        when(dao.loadContributors("foo", "bar")).thenReturn(dbData);

        LiveData<Resource<List<Contributor>>> data = repository.loadContributors("foo",
                "bar");
        verify(dao).loadContributors("foo", "bar");

        verify(service, never()).getContributors(anyString(), anyString());

        Repo repo = TestUtil.createRepo("foo", "bar", "desc");
        Contributor contributor = TestUtil.createContributor(repo, "log", 3);
        // network does not send these
        contributor.setRepoOwner(null);
        contributor.setRepoName(null);
        List<Contributor> contributors = Collections.singletonList(contributor);
        LiveData<ApiResponse<List<Contributor>>> call = successCall(contributors);
        when(service.getContributors("foo", "bar"))
                .thenReturn(call);

        Observer<Resource<List<Contributor>>> observer = mock(Observer.class);
        data.observeForever(observer);

        verify(observer).onChanged(Resource.loading( null));

        MutableLiveData<List<Contributor>> updatedDbData = new MutableLiveData<>();
        when(dao.loadContributors("foo", "bar")).thenReturn(updatedDbData);
        dbData.setValue(Collections.emptyList());

        verify(service).getContributors("foo", "bar");
        ArgumentCaptor<List<Contributor>> inserted = ArgumentCaptor.forClass((Class) List.class);
        verify(dao).insertContributors(inserted.capture());


        assertThat(inserted.getValue().size(), is(1));
        Contributor first = inserted.getValue().get(0);
        assertThat(first.getRepoName(), is("bar"));
        assertThat(first.getRepoOwner(), is("foo"));

        updatedDbData.setValue(contributors);
        verify(observer).onChanged(Resource.success(contributors));
    }

    @Test
    public void searchNextPage_null() {
        when(dao.findSearchResult()).thenReturn(null);
        Observer<Resource<Boolean>> observer = mock(Observer.class);
        repository.searchNextPage().observeForever(observer);
        verify(observer).onChanged(null);
    }

    @Test
    public void search_fromDb() {
        List<Integer> ids = Arrays.asList(1, 2);

        Observer<Resource<List<Repo>>> observer = mock(Observer.class);
        MutableLiveData<GetProjectsResult> dbSearchResult = new MutableLiveData<>();
        MutableLiveData<List<Repo>> repositories = new MutableLiveData<>();

        when(dao.search("foo")).thenReturn(dbSearchResult);

        repository.getProjects().observeForever(observer);

        verify(observer).onChanged(Resource.loading(null));
        verifyNoMoreInteractions(service);
        reset(observer);

        GetProjectsResult dbResult = new GetProjectsResult(ids, null);
        when(dao.loadOrdered(ids)).thenReturn(repositories);

        dbSearchResult.postValue(dbResult);

        List<Repo> repoList = new ArrayList<>();
        repositories.postValue(repoList);
        verify(observer).onChanged(Resource.success(repoList));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void search_fromServer() {
        List<Integer> ids = Arrays.asList(1, 2);
        Repo repo1 = TestUtil.createRepo(1, "owner", "repo 1", "desc 1");
        Repo repo2 = TestUtil.createRepo(2, "owner", "repo 2", "desc 2");

        Observer<Resource<List<Repo>>> observer = mock(Observer.class);
        MutableLiveData<GetProjectsResult> dbSearchResult = new MutableLiveData<>();
        MutableLiveData<List<Repo>> repositories = new MutableLiveData<>();

        GetProjectsResponse apiResponse = new GetProjectsResponse();
        List<Repo> repoList = Arrays.asList(repo1, repo2);
        apiResponse.setProjects(repoList);
        apiResponse.setTotal(2);

        MutableLiveData<ApiResponse<GetProjectsResponse>> callLiveData = new MutableLiveData<>();
        when(service.getProjects()).thenReturn(callLiveData);

        when(dao.search("foo")).thenReturn(dbSearchResult);

        repository.getProjects().observeForever(observer);

        verify(observer).onChanged(Resource.loading(null));
        verifyNoMoreInteractions(service);
        reset(observer);

        when(dao.loadOrdered(ids)).thenReturn(repositories);
        dbSearchResult.postValue(null);
        verify(dao, never()).loadOrdered(anyObject());

        verify(service).getProjects();
        MutableLiveData<GetProjectsResult> updatedResult = new MutableLiveData<>();
        when(dao.search("foo")).thenReturn(updatedResult);
        updatedResult.postValue(new GetProjectsResult(ids, null));

        callLiveData.postValue(new ApiResponse<>(Response.success(apiResponse)));
        verify(dao).insertRepos(repoList);
        repositories.postValue(repoList);
        verify(observer).onChanged(Resource.success(repoList));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void search_fromServer_error() {
        when(dao.search("foo")).thenReturn(AbsentLiveData.create());
        MutableLiveData<ApiResponse<GetProjectsResponse>> apiResponse = new MutableLiveData<>();
        when(service.getProjects()).thenReturn(apiResponse);

        Observer<Resource<List<Repo>>> observer = mock(Observer.class);
        repository.getProjects().observeForever(observer);
        verify(observer).onChanged(Resource.loading(null));

        apiResponse.postValue(new ApiResponse<>(new Exception("idk")));
        verify(observer).onChanged(Resource.error("idk", null));
    }
}