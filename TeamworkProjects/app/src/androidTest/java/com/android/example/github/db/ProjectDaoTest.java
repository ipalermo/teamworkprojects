
package com.android.example.github.db;

import android.arch.lifecycle.LiveData;
import android.database.sqlite.SQLiteException;
import android.support.test.runner.AndroidJUnit4;

import com.android.example.github.util.TestUtil;
import com.android.example.github.vo.Contributor;
import com.android.example.github.vo.Project;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.android.example.github.util.LiveDataTestUtil.getValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ProjectDaoTest extends DbTest {
    @Test
    public void insertAndRead() throws InterruptedException {
        Project project = TestUtil.createProject(10, "bar", "desc");
        db.projectDao().insert(project);
        Project loaded = getValue(db.projectDao().load(10));
        assertThat(loaded, notNullValue());
        assertThat(loaded.name, is("bar"));
        assertThat(loaded.description, is("desc"));
        assertThat(loaded.company, notNullValue());
        assertThat(loaded.company.id, is("foo"));
    }

    @Test
    public void insertContributorsWithoutRepo() {
        Project project = TestUtil.createProject(10, "bar", "desc");
        Contributor contributor = TestUtil.createContributor(project, "c1", 3);
        try {
            db.projectDao().insertContributors(Collections.singletonList(contributor));
            throw new AssertionError("must fail because project does not exist");
        } catch (SQLiteException ex) {}
    }

    @Test
    public void insertContributors() throws InterruptedException {
        Project project = TestUtil.createProject(10, "bar", "desc");
        Contributor c1 = TestUtil.createContributor(project, "c1", 3);
        Contributor c2 = TestUtil.createContributor(project, "c2", 7);
        db.beginTransaction();
        try {
            db.projectDao().insert(project);
            db.projectDao().insertContributors(Arrays.asList(c1, c2));
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        List<Contributor> list = getValue(db.projectDao().loadContributors(10));
        assertThat(list.size(), is(2));
        Contributor first = list.get(0);

        assertThat(first.getLogin(), is("c2"));
        assertThat(first.getContributions(), is(7));

        Contributor second = list.get(1);
        assertThat(second.getLogin(), is("c1"));
        assertThat(second.getContributions(), is(3));
    }

    @Test
    public void createIfNotExists_exists() throws InterruptedException {
        Project project = TestUtil.createProject(10, "bar", "desc");
        db.projectDao().insert(project);
        assertThat(db.projectDao().createRepoIfNotExists(project), is(-1L));
    }

    @Test
    public void createIfNotExists_doesNotExist() {
        Project project = TestUtil.createProject(10, "bar", "desc");
        assertThat(db.projectDao().createRepoIfNotExists(project), is(1L));
    }

    @Test
    public void insertContributorsThenUpdateRepo() throws InterruptedException {
        Project project = TestUtil.createProject(10, "bar", "desc");
        db.projectDao().insert(project);
        Contributor contributor = TestUtil.createContributor(project, "aa", 3);
        db.projectDao().insertContributors(Collections.singletonList(contributor));
        LiveData<List<Contributor>> data = db.projectDao().loadContributors(10);
        assertThat(getValue(data).size(), is(1));

        Project update = TestUtil.createProject(10, "bar", "desc");
        db.projectDao().insert(update);
        data = db.projectDao().loadContributors(10);
        assertThat(getValue(data).size(), is(1));
    }
}
