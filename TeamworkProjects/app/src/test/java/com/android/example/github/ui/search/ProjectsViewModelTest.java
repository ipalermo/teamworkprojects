
package com.android.example.github.ui.search;


import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import com.android.example.github.repository.ProjectRepository;
import com.android.example.github.vo.Project;
import com.android.example.github.vo.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class ProjectsViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();
    private ProjectsViewModel viewModel;
    private ProjectRepository repository;
    @Before
    public void init() {
        repository = mock(ProjectRepository.class);
        viewModel = new ProjectsViewModel(repository);
    }

    @Test
    public void empty() {
        Observer<Resource<List<Project>>> result = mock(Observer.class);
        viewModel.getResults().observeForever(result);
        viewModel.loadNextPage();
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void basic() {
        Observer<Resource<List<Project>>> result = mock(Observer.class);
        viewModel.getResults().observeForever(result);
        viewModel.setQuery("foo");
        verify(repository).getProjects();
        verify(repository, never()).projectsNextPage();
    }

    @Test
    public void noObserverNoQuery() {
        when(repository.projectsNextPage()).thenReturn(mock(LiveData.class));
        viewModel.setQuery("foo");
        verify(repository, never()).getProjects();
        // next page is user interaction and even if loading state is not observed, we query
        // would be better to avoid that if main getProjects query is not observed
        viewModel.loadNextPage();
        verify(repository).projectsNextPage();
    }

    @Test
    public void swap() {
        LiveData<Resource<Boolean>> nextPage = new MutableLiveData<>();
        when(repository.projectsNextPage()).thenReturn(nextPage);

        Observer<Resource<List<Project>>> result = mock(Observer.class);
        viewModel.getResults().observeForever(result);
        verifyNoMoreInteractions(repository);
        viewModel.setQuery("foo");
        verify(repository).getProjects();
        viewModel.loadNextPage();

        viewModel.getLoadMoreStatus().observeForever(mock(Observer.class));
        verify(repository).projectsNextPage();
        assertThat(nextPage.hasActiveObservers(), is(true));
        viewModel.setQuery("bar");
        assertThat(nextPage.hasActiveObservers(), is(false));
        verify(repository).getProjects();
        verify(repository, never()).projectsNextPage();
    }

    @Test
    public void refresh() {
        viewModel.loadProjects();
        verifyNoMoreInteractions(repository);
        viewModel.setQuery("foo");
        viewModel.loadProjects();
        verifyNoMoreInteractions(repository);
        viewModel.getResults().observeForever(mock(Observer.class));
        verify(repository).getProjects();
        reset(repository);
        viewModel.loadProjects();
        verify(repository).getProjects();
    }

    @Test
    public void resetSameQuery() {
        viewModel.getResults().observeForever(mock(Observer.class));
        viewModel.setQuery("foo");
        verify(repository).getProjects();
        reset(repository);
        viewModel.setQuery("FOO");
        verifyNoMoreInteractions(repository);
        viewModel.setQuery("bar");
        verify(repository).getProjects();
    }
}