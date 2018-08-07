
package com.android.example.github.vo;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Using name/owner_login as primary key instead of id since name/owner_login is always available
 * vs id is not.
 */
@Entity(indices = {@Index("name")})
public class Project {
    public static final int UNKNOWN_ID = -1;
    @PrimaryKey
    public final int id;
    @SerializedName("name")
    @NonNull
    public final String name;
    @SerializedName("status")
    public String status;
    @SerializedName("subStatus")
    public String subStatus;
    @SerializedName("created-on")
    public String createdOn;
    @SerializedName("logo")
    public String logo;
    @SerializedName("startDate")
    public String startDate;
    @SerializedName("endDate")
    public String endDate;
    @SerializedName("description")
    public final String description;
    @SerializedName("starred")
    public final boolean starred;
    @SerializedName("company")
    @Embedded(prefix = "company_")
    @NonNull
    public final Company company;

    public Project(int id, String name, String description, Company company, boolean starred) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.company = company;
        this.starred = starred;
    }

    public static class Company {
        @SerializedName("id")
        @NonNull
        public final String id;
        @SerializedName("name")
        public final String name;

        @Ignore
        public Company(String name) {
            this(UUID.randomUUID().toString(), name);
        }

        public Company(@NonNull String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Company company = (Company) o;

            if (id != null ? !id.equals(company.id) : company.id != null) {
                return false;
            }
            return name != null ? name.equals(company.name) : company.name == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }
}
