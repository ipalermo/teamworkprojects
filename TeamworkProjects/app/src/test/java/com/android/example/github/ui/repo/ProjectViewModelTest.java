
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
import static org.mockito.ArgumentMatchers.anyInt;
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
        verify(repository, never()).loadProject(anyInt());
    }

    @Test
    public void dontFetchWithoutObservers() {
        projectViewModel.setId(10);
        verify(repository, never()).loadProject(anyInt());
    }

    @Test
    public void fetchWhenObserved() {
        ArgumentCaptor<Integer> id = ArgumentCaptor.forClass(Integer.class);

        projectViewModel.setId(10);
        projectViewModel.getProject().observeForever(mock(Observer.class));
        verify(repository, times(1)).loadProject(
                id.capture());
        assertThat(id.getValue(), is(10));
    }

    @Test
    public void changeWhileObserved() {
        ArgumentCaptor<Integer> id = ArgumentCaptor.forClass(Integer.class);
        projectViewModel.getProject().observeForever(mock(Observer.class));

        projectViewModel.setId(10);
        projectViewModel.setId(15);

        verify(repository, times(2)).loadProject(
                id.capture());
        assertThat(id.getAllValues(), is(Arrays.asList(10, 15)));
    }

    @Test
    public void contributors() {
        Observer<Resource<List<Contributor>>> observer = mock(Observer.class);
        projectViewModel.getContributors().observeForever(observer);
        verifyNoMoreInteractions(observer);
        verifyNoMoreInteractions(repository);
        projectViewModel.setId(10);
        verify(repository).loadContributors(10);
    }

    @Test
    public void resetId() {
        Observer<Integer> observer = mock(Observer.class);
        projectViewModel.projectId.observeForever(observer);
        verifyNoMoreInteractions(observer);
        projectViewModel.setId(10);
        verify(observer).onChanged(10);
        reset(observer);
        projectViewModel.setId(10);
        verifyNoMoreInteractions(observer);
        projectViewModel.setId(15);
        verify(observer).onChanged(15);
    }

    @Test
    public void retry() {
        projectViewModel.retry();
        verifyNoMoreInteractions(repository);
        projectViewModel.setId(10);
        verifyNoMoreInteractions(repository);
        Observer<Resource<Project>> observer = mock(Observer.class);
        projectViewModel.getProject().observeForever(observer);
        verify(repository).loadProject(10);
        reset(repository);
        projectViewModel.retry();
        verify(repository).loadProject(10);
    }

    @Test
    public void nullProjectId() {
        projectViewModel.setId(null);
        Observer<Resource<Project>> observer1 = mock(Observer.class);
        Observer<Resource<List<Contributor>>> observer2 = mock(Observer.class);
        projectViewModel.getProject().observeForever(observer1);
        projectViewModel.getContributors().observeForever(observer2);
        verify(observer1).onChanged(null);
        verify(observer2).onChanged(null);
    }
}