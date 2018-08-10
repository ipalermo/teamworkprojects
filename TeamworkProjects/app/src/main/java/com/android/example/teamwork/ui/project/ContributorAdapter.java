
package com.android.example.teamwork.ui.project;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.example.teamwork.R;
import com.android.example.teamwork.databinding.ContributorItemBinding;
import com.android.example.teamwork.ui.common.DataBoundListAdapter;
import com.android.example.teamwork.util.Objects;
import com.android.example.teamwork.vo.Contributor;

public class ContributorAdapter
        extends DataBoundListAdapter<Contributor, ContributorItemBinding> {

    private final DataBindingComponent dataBindingComponent;
    private final ContributorClickCallback callback;

    public ContributorAdapter(DataBindingComponent dataBindingComponent,
            ContributorClickCallback callback) {
        this.dataBindingComponent = dataBindingComponent;
        this.callback = callback;
    }

    @Override
    protected ContributorItemBinding createBinding(ViewGroup parent) {
        ContributorItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.contributor_item, parent, false,
                        dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            Contributor contributor = binding.getContributor();
            if (contributor != null && callback != null) {
                callback.onClick(contributor);
            }
        });
        return binding;
    }

    @Override
    protected void bind(ContributorItemBinding binding, Contributor item) {
        binding.setContributor(item);
    }

    @Override
    protected boolean areItemsTheSame(Contributor oldItem, Contributor newItem) {
        return Objects.equals(oldItem.getLogin(), newItem.getLogin());
    }

    @Override
    protected boolean areContentsTheSame(Contributor oldItem, Contributor newItem) {
        return Objects.equals(oldItem.getAvatarUrl(), newItem.getAvatarUrl())
                && oldItem.getContributions() == newItem.getContributions();
    }

    public interface ContributorClickCallback {
        void onClick(Contributor contributor);
    }
}
