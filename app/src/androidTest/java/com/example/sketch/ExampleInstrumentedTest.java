package com.example.sketch;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.example.sketch.DatabaseOperations;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.sketch", appContext.getPackageName());
        DataBaseTester tester = new DataBaseTester(appContext);

        tester.open();

        // Insert test user
        long userId = tester.insertUser("Jane Doe", "jane@example.com");

        // Insert test canvas
        tester.insertCanvas(userId, "My First Sketch", "Canvas data in string or byte format");

        // Retrieve and display all users and canvases
        tester.getAllUsers();
        tester.getAllCanvases();

        tester.close();

        DatabaseOperations dbOperations = new DatabaseOperations(appContext);
        dbOperations.open();

        dbOperations.insertCanvas(userId, "Sketch 1", "Canvas Data");

        dbOperations.close();

    }
}