package com.example.background.workers

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented Test on [BlurWorker]
 *
 * @author Kaushik N Sanji
 */
class BlurWorkerTest {

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
     * Test method to verify if the [BlurWorker] fails when there is no input URI.
     */
    @Test
    fun testFailsIfNoInput() {
        // Create WorkRequest for BlurWorker
        val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>().build()

        // Enqueue and wait for the WorkRequest result
        // Since we are using SynchronousExecutor, this Worker executes synchronously
        workManagerRule.workManager.enqueue(blurRequest).result.get()

        // Get WorkInfo of the WorkRequest
        val workInfo = workManagerRule.workManager.getWorkInfoById(blurRequest.id).get()

        // Assert that the BlurWorker has failed
        assertThat(workInfo.state, `is`(WorkInfo.State.FAILED))
    }

    /**
     * Test method to verify if the [BlurWorker] applies blur when given a valid Image URI.
     */
    @Test
    fun testAppliesBlur() {
        // Copy the Test Image from the assets folder to the OUTPUT_PATH on the device
        val imageUri = copyFileFromTestToTarget(
                workManagerRule.testContext,
                workManagerRule.targetContext,
                "test_image.jpg"
        )

        // Create WorkRequest for BlurWorker
        val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
                // Input Data containing the Image to blur
                .setInputData(workDataOf(KEY_IMAGE_URI to imageUri.toString()))
                .build()

        // Enqueue and wait for the WorkRequest result
        // Since we are using SynchronousExecutor, this Worker executes synchronously
        workManagerRule.workManager.enqueue(blurRequest).result.get()

        // Get WorkInfo of the WorkRequest
        val workInfo = workManagerRule.workManager.getWorkInfoById(blurRequest.id).get()

        // Get the Output Blurred Image URI from the WorkInfo
        val blurredImageUriStr = workInfo.outputData.getString(KEY_IMAGE_URI)

        // Assert that the Output Blurred Image is created
        assertThat(uriFileExists(workManagerRule.targetContext, blurredImageUriStr), `is`(true))
        // Assert that the BlurWorker has finished successfully
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
    }
}