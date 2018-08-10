
package com.android.example.teamwork.db;


import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;

abstract public class DbTest {
    protected TeamworkDb db;

    @Before
    public void initDb() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                TeamworkDb.class).build();
    }

    @After
    public void closeDb() {
        db.close();
    }
}
