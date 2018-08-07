
package com.android.example.github.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(primaryKeys = {"projectId"},
        foreignKeys = @ForeignKey(entity = Project.class,
                parentColumns = {"id"},
                childColumns = {"projectId"},
                onUpdate = ForeignKey.CASCADE,
                deferred = true))
public class Contributor {

    @SerializedName("login")
    @NonNull
    private final String login;

    @SerializedName("contributions")
    private final int contributions;

    @SerializedName("avatar_url")
    private final String avatarUrl;

    @NonNull
    private int projectId;

    public Contributor(String login, int contributions, String avatarUrl) {
        this.login = login;
        this.contributions = contributions;
        this.avatarUrl = avatarUrl;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getLogin() {
        return login;
    }

    public int getContributions() {
        return contributions;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public int getProjectId() {
        return projectId;
    }
}
