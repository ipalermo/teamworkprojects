
package com.android.example.teamwork.ui.projectslist;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.MutableLiveData;

import com.android.example.teamwork.repository.ProjectRepository;
import com.android.example.teamwork.vo.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class NextPageHandlerTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private ProjectsViewModel.NextPageHandler pageHandler;

    private ProjectRepository repository;

    @Before
    public void init() {
        repository = mock(ProjectRepository.class);
        pageHandler = new ProjectsViewModel.NextPageHandler(repository);
    }

    @Test
    public void constructor() {
        ProjectsViewModel.LoadMoreState initial = getStatus();
        assertThat(initial, notNullValue());
        assertThat(initial.isRunning(), is(false));
        assertThat(initial.getErrorMessage(), nullValue());
    }

    @Test
    public void reloadSameValue() {
        enqueueResponse();
        pageHandler.queryNextPage();
        verify(repository).projectsNextPage();

        reset(repository);
        pageHandler.queryNextPage();
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void success() {
        MutableLiveData<Resource<Boolean>> liveData = enqueueResponse();

        pageHandler.queryNextPage();
        verify(repository).projectsNextPage();
        assertThat(liveData.hasActiveObservers(), is(true));
        pageHandler.onChanged(Resource.loading(null));
        assertThat(liveData.hasActiveObservers(), is(true));
        assertThat(getStatus().isRunning(), is(true));

        pageHandler.onChanged(Resource.success(true));
        assertThat(liveData.hasActiveObservers(), is(false));
        assertThat(pageHandler.hasMore, is(true));
        assertThat(getStatus().isRunning(), is(false));
        assertThat(liveData.hasActiveObservers(), is(false));

        // requery
        reset(repository);
        MutableLiveData<Resource<Boolean>> nextPage = enqueueResponse();
        pageHandler.queryNextPage();
        verify(repository).projectsNextPage();
        assertThat(nextPage.hasActiveObservers(), is(true));

        pageHandler.onChanged(Resource.success(false));
        assertThat(liveData.hasActiveObservers(), is(false));
        assertThat(pageHandler.hasMore, is(false));
        assertThat(getStatus().isRunning(), is(false));
        assertThat(nextPage.hasActiveObservers(), is(false));

        // retry, no query
        reset(repository);
        pageHandler.queryNextPage();
        verifyNoMoreInteractions(repository);
        pageHandler.queryNextPage();
        verifyNoMoreInteractions(repository);

        // query another
        MutableLiveData<Resource<Boolean>> bar = enqueueResponse();
        pageHandler.queryNextPage();
        verify(repository).projectsNextPage();
        assertThat(bar.hasActiveObservers(), is(true));
    }

    @Test
    public void failure() {
        MutableLiveData<Resource<Boolean>> liveData = enqueueResponse();
        pageHandler.queryNextPage();
        assertThat(liveData.hasActiveObservers(), is(true));
        pageHandler.onChanged(Resource.error("idk", false));
        assertThat(liveData.hasActiveObservers(), is(false));
        assertThat(getStatus().getErrorMessage(), is("idk"));
        assertThat(getStatus().getErrorMessageIfNotHandled(), is("idk"));
        assertThat(getStatus().getErrorMessageIfNotHandled(), nullValue());
        assertThat(getStatus().isRunning(), is(false));
        assertThat(pageHandler.hasMore, is(true));

        reset(repository);
        MutableLiveData<Resource<Boolean>> liveData2 = enqueueResponse();
        pageHandler.queryNextPage();
        assertThat(liveData2.hasActiveObservers(), is(true));
        assertThat(getStatus().isRunning(), is(true));
        pageHandler.onChanged(Resource.success(false));
        assertThat(getStatus().isRunning(), is(false));
        assertThat(getStatus().getErrorMessage(), is(nullValue()));
        assertThat(pageHandler.hasMore, is(false));
    }

    @Test
    public void nullOnChanged() {
        MutableLiveData<Resource<Boolean>> liveData = enqueueResponse();
        pageHandler.queryNextPage();
        assertThat(liveData.hasActiveObservers(), is(true));
        pageHandler.onChanged(null);
        assertThat(liveData.hasActiveObservers(), is(false));
    }

    private ProjectsViewModel.LoadMoreState getStatus() {
        return pageHandler.getLoadMoreState().getValue();
    }

    private MutableLiveData<Resource<Boolean>> enqueueResponse() {
        MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        when(repository.projectsNextPage()).thenReturn(liveData);
        return liveData;
    }
}