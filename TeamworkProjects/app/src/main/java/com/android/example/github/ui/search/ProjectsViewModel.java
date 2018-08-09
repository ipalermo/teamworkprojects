
package com.android.example.github.ui.search;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.android.example.github.repository.ProjectRepository;
import com.android.example.github.vo.Project;
import com.android.example.github.vo.Resource;

import java.util.List;

import javax.inject.Inject;

public class ProjectsViewModel extends ViewModel {

    private LiveData<Resource<List<Project>>> results;

    private final NextPageHandler nextPageHandler;

    private ProjectRepository projectsRepository;

    @Inject
    ProjectsViewModel(ProjectRepository projectRepository) {
        nextPageHandler = new NextPageHandler(projectRepository);
        this.projectsRepository = projectRepository;
        loadProjects();
    }

    @VisibleForTesting
    public LiveData<Resource<List<Project>>> getResults() {
        return results;
    }

    @VisibleForTesting
    public LiveData<LoadMoreState> getLoadMoreStatus() {
        return nextPageHandler.getLoadMoreState();
    }

    @VisibleForTesting
    public void loadNextPage() {
        nextPageHandler.queryNextPage();
    }

    public void loadProjects() {
        results =  projectsRepository.getProjects();
    }

    static class LoadMoreState {
        private final boolean running;
        private final String errorMessage;
        private boolean handledError = false;

        LoadMoreState(boolean running, String errorMessage) {
            this.running = running;
            this.errorMessage = errorMessage;
        }

        boolean isRunning() {
            return running;
        }

        String getErrorMessage() {
            return errorMessage;
        }

        String getErrorMessageIfNotHandled() {
            if (handledError) {
                return null;
            }
            handledError = true;
            return errorMessage;
        }
    }

    @VisibleForTesting
    static class NextPageHandler implements Observer<Resource<Boolean>> {
        @Nullable
        private LiveData<Resource<Boolean>> nextPageLiveData;
        private final MutableLiveData<LoadMoreState> loadMoreState = new MutableLiveData<>();
        private final ProjectRepository repository;
        @VisibleForTesting
        boolean hasMore;

        @VisibleForTesting
        NextPageHandler(ProjectRepository repository) {
            this.repository = repository;
            reset();
        }

        void queryNextPage() {
            unregister();
            nextPageLiveData = repository.projectsNextPage();
            loadMoreState.setValue(new LoadMoreState(true, null));
            //noinspection ConstantConditions
            nextPageLiveData.observeForever(this);
        }

        @Override
        public void onChanged(@Nullable Resource<Boolean> result) {
            if (result == null) {
                reset();
            } else {
                switch (result.status) {
                    case SUCCESS:
                        hasMore = Boolean.TRUE.equals(result.data);
                        unregister();
                        loadMoreState.setValue(new LoadMoreState(false, null));
                        break;
                    case ERROR:
                        hasMore = true;
                        unregister();
                        loadMoreState.setValue(new LoadMoreState(false,
                                result.message));
                        break;
                }
            }
        }

        private void unregister() {
            if (nextPageLiveData != null) {
                nextPageLiveData.removeObserver(this);
                nextPageLiveData = null;
            }
        }

        private void reset() {
            unregister();
            hasMore = true;
            loadMoreState.setValue(new LoadMoreState(false, null));
        }

        MutableLiveData<LoadMoreState> getLoadMoreState() {
            return loadMoreState;
        }
    }
}
