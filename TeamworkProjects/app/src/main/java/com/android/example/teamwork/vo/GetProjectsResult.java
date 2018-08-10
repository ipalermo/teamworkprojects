
package com.android.example.teamwork.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.Nullable;

import com.android.example.teamwork.db.TeamworkTypeConverters;

import java.util.List;

@Entity
@TypeConverters(TeamworkTypeConverters.class)
public class GetProjectsResult {

    @PrimaryKey(autoGenerate = true)
    public Integer Id;

    public final List<Integer> projectIds;
    @Nullable
    public final Integer next;

    public GetProjectsResult(List<Integer> projectIds,
                             @Nullable Integer next) {
        this.projectIds = projectIds;
        this.next = next;
    }
}
