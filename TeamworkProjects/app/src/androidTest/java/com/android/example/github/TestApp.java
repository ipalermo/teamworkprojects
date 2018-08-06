
package com.android.example.github;

import android.app.Application;

/**
 * We use a separate App for tests to prevent initializing dependency injection.
 *
 * See {@link com.android.example.github.util.GithubTestRunner}.
 */
public class TestApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
