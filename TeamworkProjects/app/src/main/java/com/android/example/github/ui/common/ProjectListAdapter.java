
package com.android.example.github.ui.common;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.example.github.R;
import com.android.example.github.databinding.ProjectItemBinding;
import com.android.example.github.util.Objects;
import com.android.example.github.vo.Project;

/**
 * A RecyclerView adapter for {@link Project} class.
 */
public class ProjectListAdapter extends DataBoundListAdapter<Project, ProjectItemBinding> {
    private final DataBindingComponent dataBindingComponent;
    private final ProjectClickCallback projectClickCallback;

    public ProjectListAdapter(DataBindingComponent dataBindingComponent,
                              ProjectClickCallback projectClickCallback) {
        this.dataBindingComponent = dataBindingComponent;
        this.projectClickCallback = projectClickCallback;
    }

    @Override
    protected ProjectItemBinding createBinding(ViewGroup parent) {
        ProjectItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.project_item,
                        parent, false, dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            Project project = binding.getProject();
            if (project != null && projectClickCallback != null) {
                projectClickCallback.onClick(project);
            }
        });
        return binding;
    }

    @Override
    protected void bind(ProjectItemBinding binding, Project item) {
        binding.setProject(item);
    }

    @Override
    protected boolean areItemsTheSame(Project oldItem, Project newItem) {
        return Objects.equals(oldItem.id, newItem.id);
    }

    @Override
    protected boolean areContentsTheSame(Project oldItem, Project newItem) {
        return Objects.equals(oldItem.description, newItem.description) &&
                oldItem.name == newItem.name;
    }

    public interface ProjectClickCallback {
        void onClick(Project project);
    }
}
