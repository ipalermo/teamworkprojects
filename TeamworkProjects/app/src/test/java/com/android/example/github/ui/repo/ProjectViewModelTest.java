
package com.android.example.github.ui.repo;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;

import com.android.example.github.repository.ProjectRepository;
import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.Project;
import com.android.example.github.vo.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(JUnit4.class)
public class ProjectViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private ProjectRepository repository;
    private ProjectViewModel projectViewModel;

    @Before
    public void setup() {
        repository = mock(ProjectRepository.class);
        projectViewModel = new ProjectViewModel(repository);
    }


    @Test
    public void testNull() {
        assertThat(projectViewModel.getProject(), notNullValue());
        assertThat(projectViewModel.getContributors(), notNullValue());
        verify(repository, never()).loadProject(anyString());
    }

    @Test
    public void dontFetchWithoutObservers() {
        projectViewModel.setId("a", "b");
        verify(repository, never()).loadProject(anyString());
    }

    @Test
    public void fetchWhenObserved() {
        ArgumentCaptor<String> owner = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);

        projectViewModel.setId("a", "b");
        projectViewModel.getProject().observeForever(mock(Observer.class));
        verify(repository, times(1)).loadProject(
                name.capture());
        assertThat(owner.getValue(), is("a"));
        assertThat(name.getValue(), is("b"));
    }

    @Test
    public void changeWhileObserved() {
        ArgumentCaptor<String> owner = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);
        projectViewModel.getProject().observeForever(mock(Observer.class));

        projectViewModel.setId("a", "b");
        projectViewModel.setId("c", "d");

        verify(repository, times(2)).loadProject(
                name.capture());
        assertThat(owner.getAllValues(), is(Arrays.asList("a", "c")));
        assertThat(name.getAllValues(), is(Arrays.asList("b", "d")));
    }

    @Test
    public void contributors() {
        Observer<Resource<List<Contributor>>> observer = mock(Observer.class);
        projectViewModel.getContributors().observeForever(observer);
        verifyNoMoreInteractions(observer);
        verifyNoMoreInteractions(repository);
        projectViewModel.setId("foo", "bar");
        verify(repository).loadContributors("bar");
    }

    @Test
    public void resetId() {
        Observer<ProjectViewModel.RepoId> observer = mock(Observer.class);
        projectViewModel.projectId.observeForever(observer);
        verifyNoMoreInteractions(observer);
        projectViewModel.setId("foo", "bar");
        verify(observer).onChanged(new ProjectViewModel.RepoId("foo", "bar"));
        reset(observer);
        projectViewModel.setId("foo", "bar");
        verifyNoMoreInteractions(observer);
        projectViewModel.setId("a", "b");
        verify(observer).onChanged(new ProjectViewModel.RepoId("a", "b"));
    }

    @Test
    public void retry() {
        projectViewModel.retry();
        verifyNoMoreInteractions(repository);
        projectViewModel.setId("foo", "bar");
        verifyNoMoreInteractions(repository);
        Observer<Resource<Project>> observer = mock(Observer.class);
        projectViewModel.getProject().observeForever(observer);
        verify(repository).loadProject("bar");
        reset(repository);
        projectViewModel.retry();
        verify(repository).loadProject("bar");
    }

    @Test
    public void nullRepoId() {
        projectViewModel.setId(null, null);
        Observer<Resource<Project>> observer1 = mock(Observer.class);
        Observer<Resource<List<Contributor>>> observer2 = mock(Observer.class);
        projectViewModel.getProject().observeForever(observer1);
        projectViewModel.getContributors().observeForever(observer2);
        verify(observer1).onChanged(null);
        verify(observer2).onChanged(null);
    }
}