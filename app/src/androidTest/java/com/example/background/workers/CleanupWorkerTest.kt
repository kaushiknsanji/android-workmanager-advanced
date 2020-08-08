package com.example.background.workers

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented Test on [CleanupWorker]
 *
 * @author Kaushik N Sanji
 */
class CleanupWorkerTest {

    /**
     * Getter for [org.junit.rules.TestRule] that returns [InstantTaskExecutorRule].
     * [InstantTaskExecutorRule] ensures that `set` and `post` actions of [androidx.lifecycle.LiveData]
     * happens on the same Thread synchronously, for Testing purpose.
     */
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * Getter for [org.junit.rules.TestRule] that returns [WorkManagerTestRule].
     * [WorkManagerTestRule] provides [androidx.work.WorkManager] that uses a
     * [androidx.work.testing.SynchronousExecutor] to execute Workers synchronously,
     * for Testing purpose.
     */
    @get:Rule
    val workManagerRule = WorkManagerTestRule()

    /**
     * Test method to verify if [CleanupWorker] deletes the temporary files.
     */
    @Test
    fun testCleanupWork() {
        // Copy the Test Image from the assets folder to the OUTPUT_PATH on the device
        val testUri = copyFileFromTestToTarget(
                workManagerRule.testContext,
                workManagerRule.targetContext,
                "test_image.jpg"
        )
        // Assert that the temporary file is copied to the device
        assertThat(uriFileExists(workManagerRule.targetContext, testUri.toString()), `is`(true))

        // Create WorkRequest for CleanupWorker
        val cleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>()
                .build()

        // Enqueue and wait for the WorkRequest result
        // Since we are using SynchronousExecutor, this Worker executes synchronously
        workManagerRule.workManager.enqueue(cleanupRequest).result.get()

        // Get WorkInfo of the WorkRequest
        val workInfo = workManagerRule.workManager.getWorkInfoById(cleanupRequest.id).get()

        // Assert that the temporary file copied is deleted
        assertThat(uriFileExists(workManagerRule.targetContext, testUri.toString()), `is`(false))
        // Assert that the Cleanup Work has finished successfully
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
    }

}