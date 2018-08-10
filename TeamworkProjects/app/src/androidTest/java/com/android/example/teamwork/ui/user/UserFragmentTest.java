
package com.android.example.teamwork.ui.user;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.android.example.teamwork.R;
import com.android.example.teamwork.binding.FragmentBindingAdapters;
import com.android.example.teamwork.testing.SingleFragmentActivity;
import com.android.example.teamwork.ui.common.NavigationController;
import com.android.example.teamwork.util.EspressoTestUtil;
import com.android.example.teamwork.util.RecyclerViewMatcher;
import com.android.example.teamwork.util.TestUtil;
import com.android.example.teamwork.util.ViewModelUtil;
import com.android.example.teamwork.vo.Project;
import com.android.example.teamwork.vo.Resource;
import com.android.example.teamwork.vo.User;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class UserFragmentTest {
    @Rule
    public ActivityTestRule<SingleFragmentActivity> activityRule =
            new ActivityTestRule<>(SingleFragmentActivity.class, true, true);

    private UserViewModel viewModel;
    private NavigationController navigationController;
    private FragmentBindingAdapters fragmentBindingAdapters;
    private MutableLiveData<Resource<User>> userData = new MutableLiveData<>();
    private MutableLiveData<Resource<List<Project>>> repoListData = new MutableLiveData<>();

    @Before
    public void init() throws Throwable {
        EspressoTestUtil.disableProgressBarAnimations(activityRule);
        UserFragment fragment = UserFragment.create("foo");
        viewModel = mock(UserViewModel.class);
        when(viewModel.getUser()).thenReturn(userData);
        when(viewModel.getRepositories()).thenReturn(repoListData);
        doNothing().when(viewModel).setLogin(anyString());
        navigationController = mock(NavigationController.class);
        fragmentBindingAdapters = mock(FragmentBindingAdapters.class);

        fragment.viewModelFactory = ViewModelUtil.createFor(viewModel);
        fragment.navigationController = navigationController;
        fragment.dataBindingComponent = () -> fragmentBindingAdapters;

        activityRule.getActivity().setFragment(fragment);
        activityRule.runOnUiThread(() -> fragment.binding.get().repoList.setItemAnimator(null));
    }

    @Test
    public void loading() {
        userData.postValue(Resource.loading(null));
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.retry)).check(matches(not(isDisplayed())));
    }

    @Test
    public void error() throws InterruptedException {
        doNothing().when(viewModel).retry();
        userData.postValue(Resource.error("wtf", null));
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
        onView(withId(R.id.error_msg)).check(matches(withText("wtf")));
        onView(withId(R.id.retry)).check(matches(isDisplayed()));
        onView(withId(R.id.retry)).perform(click());
        verify(viewModel).retry();
    }

    @Test
    public void loadingWithUser() {
        User user = TestUtil.createUser("foo");
        userData.postValue(Resource.loading(user));
        onView(withId(R.id.name)).check(matches(withText(user.name)));
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
    }

    @Test
    public void loadedUser() {
        User user = TestUtil.createUser("foo");
        userData.postValue(Resource.success(user));
        onView(withId(R.id.name)).check(matches(withText(user.name)));
        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
    }

    @Test
    public void loadRepos() {
        List<Project> projects = setRepos(2);
        for (int pos = 0; pos < projects.size(); pos ++) {
            Project project = projects.get(pos);
            onView(listMatcher().atPosition(pos)).check(
                    matches(hasDescendant(withText(project.name))));
            onView(listMatcher().atPosition(pos)).check(
                    matches(hasDescendant(withText(project.description))));
            onView(listMatcher().atPosition(pos)).check(
                    matches(hasDescendant(withText("" + project.starred))));
        }
        Project project3 = setRepos(3).get(2);
        onView(listMatcher().atPosition(2)).check(
                matches(hasDescendant(withText(project3.name))));
    }

    @Test
    public void clickRepo() {
        List<Project> projects = setRepos(2);
        Project selected = projects.get(1);
        onView(withText(selected.description)).perform(click());
        verify(navigationController).navigateToProject(selected.name);
    }

    @Test
    public void nullUser() {
        userData.postValue(null);
        onView(withId(R.id.name)).check(matches(not(isDisplayed())));
    }

    @Test
    public void nullRepoList() {
        repoListData.postValue(null);
        onView(listMatcher().atPosition(0)).check(doesNotExist());
    }

    @Test
    public void nulledUser() {
        User user = TestUtil.createUser("foo");
        userData.postValue(Resource.success(user));
        onView(withId(R.id.name)).check(matches(withText(user.name)));
        userData.postValue(null);
        onView(withId(R.id.name)).check(matches(not(isDisplayed())));
    }

    @Test
    public void nulledRepoList() {
        setRepos(5);
        onView(listMatcher().atPosition(1)).check(matches(isDisplayed()));
        repoListData.postValue(null);
        onView(listMatcher().atPosition(0)).check(doesNotExist());
    }

    @NonNull
    private RecyclerViewMatcher listMatcher() {
        return new RecyclerViewMatcher(R.id.project_list);
    }

    private List<Project> setRepos(int count) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            projects.add(TestUtil.createProject("foo", "name " + i, "desc" + i));
        }
        repoListData.postValue(Resource.success(projects));
        return projects;
    }
}