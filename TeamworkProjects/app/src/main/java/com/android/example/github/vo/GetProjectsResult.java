
package com.android.example.github.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.Nullable;

import com.android.example.github.db.GithubTypeConverters;

import java.util.List;

@Entity
@TypeConverters(GithubTypeConverters.class)
public class GetProjectsResult {

    @PrimaryKey(autoGenerate = true)
    public Integer Id;

    public final List<Integer> repoIds;
    @Nullable
    public final Integer next;

    public GetProjectsResult(List<Integer> repoIds,
                             @Nullable Integer next) {
        this.repoIds = repoIds;
        this.next = next;
    }
}
