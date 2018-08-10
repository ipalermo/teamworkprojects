
package com.android.example.teamwork;

import android.app.Application;

import com.android.example.teamwork.util.CustomTestRunner;

/**
 * We use a separate App for tests to prevent initializing dependency injection.
 *
 * See {@link CustomTestRunner}.
 */
public class TestApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
