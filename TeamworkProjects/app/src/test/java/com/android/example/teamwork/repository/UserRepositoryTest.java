
package com.android.example.teamwork.repository;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import com.android.example.teamwork.api.ApiResponse;
import com.android.example.teamwork.api.TeamworkService;
import com.android.example.teamwork.db.UserDao;
import com.android.example.teamwork.util.ApiUtil;
import com.android.example.teamwork.util.InstantAppExecutors;
import com.android.example.teamwork.util.TestUtil;
import com.android.example.teamwork.vo.Resource;
import com.android.example.teamwork.vo.User;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class UserRepositoryTest {
    private UserDao userDao;
    private TeamworkService teamworkService;
    private UserRepository repo;

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setup() {
        userDao = mock(UserDao.class);
        teamworkService = mock(TeamworkService.class);
        repo = new UserRepository(new InstantAppExecutors(), userDao, teamworkService);
    }

    @Test
    public void loadUser() {
        repo.loadUser("abc");
        verify(userDao).findByLogin("abc");
    }

    @Test
    public void goToNetwork() {
        MutableLiveData<User> dbData = new MutableLiveData<>();
        when(userDao.findByLogin("foo")).thenReturn(dbData);
        User user = TestUtil.createUser("foo");
        LiveData<ApiResponse<User>> call = ApiUtil.successCall(user);
        when(teamworkService.getUser("foo")).thenReturn(call);
        Observer<Resource<User>> observer = mock(Observer.class);

        repo.loadUser("foo").observeForever(observer);
        verify(teamworkService, never()).getUser("foo");
        MutableLiveData<User> updatedDbData = new MutableLiveData<>();
        when(userDao.findByLogin("foo")).thenReturn(updatedDbData);
        dbData.setValue(null);
        verify(teamworkService).getUser("foo");
    }

    @Test
    public void dontGoToNetwork() {
        MutableLiveData<User> dbData = new MutableLiveData<>();
        User user = TestUtil.createUser("foo");
        dbData.setValue(user);
        when(userDao.findByLogin("foo")).thenReturn(dbData);
        Observer<Resource<User>> observer = mock(Observer.class);
        repo.loadUser("foo").observeForever(observer);
        verify(teamworkService, never()).getUser("foo");
        verify(observer).onChanged(Resource.success(user));
    }
}