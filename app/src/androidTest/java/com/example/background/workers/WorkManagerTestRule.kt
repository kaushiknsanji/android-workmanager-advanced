package com.example.background.workers

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * [org.junit.rules.TestRule] for the setup of [WorkManager] for testing.
 *
 * @author Kaushik N Sanji
 */
class WorkManagerTestRule : TestWatcher() {
    // Application Context
    lateinit var targetContext: Context

    // Instrumented Context for testing
    lateinit var testContext: Context

    // WorkManager instance
    lateinit var workManager: WorkManager

    /**
     * Invoked when a test is about to start
     */
    override fun starting(description: Description?) {
        // Get the Application Context
        targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        // Get the Instrumented Context
        testContext = InstrumentationRegistry.getInstrumentation().context

        // Initialize the Configuration for WorkManager
        val configuration = Configuration.Builder()
                // Use Logging Level of DEBUG
                .setMinimumLoggingLevel(Log.DEBUG)
                // Use SynchronousExecutor to make it easier for testing, by running Workers synchronously
                .setExecutor(SynchronousExecutor())
                .build()

        // Initialize WorkManager for Instrumentation tests with the above Configuration
        WorkManagerTestInitHelper.initializeTestWorkManager(targetContext, configuration)
        workManager = WorkManager.getInstance(targetContext)
    }
}