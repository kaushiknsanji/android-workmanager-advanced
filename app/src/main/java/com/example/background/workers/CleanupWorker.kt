package com.example.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import timber.log.Timber

/**
 * [androidx.work.ListenableWorker] subclass that implements [Worker.doWork] to provide the code
 * for cleaning up any temporary image files from the application's files' subdirectory [com.example.background.OUTPUT_PATH].
 *
 * @param context The application [Context]
 * @param workerParameters Parameters to setup the internal state of this worker
 *
 * @author Kaushik N Sanji
 */
class CleanupWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    /**
     * Override this method to do your actual background processing.  This method is called on a
     * background thread - you are required to **synchronously** do your work and return the
     * [androidx.work.ListenableWorker.Result] from this method.  Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed. If
     * you need to do your work asynchronously on a thread of your own choice, see
     * [androidx.work.ListenableWorker].
     *
     * A Worker is given a maximum of ten minutes to finish its execution and return a
     * [androidx.work.ListenableWorker.Result].  After this time has expired, the Worker will
     * be signalled to stop.
     *
     * @return The [androidx.work.ListenableWorker.Result] of the computation; note that
     * dependent work will not execute if you use
     * [androidx.work.ListenableWorker.Result.failure] or
     * [androidx.work.ListenableWorker.Result.failure]
     */
    override fun doWork(): Result {
        // Show a Notification when the work starts
        makeStatusNotification(applicationContext, "Cleaning up old temporary files")
        // Slow down the start so that it is easier to see each WorkRequest start
        sleep()

        return try {
            // Clean up any temporary image files if present
            cleanUpTempFiles(applicationContext)
            // Return as successful
            Result.success()
        } catch (throwable: Throwable) {
            // Log the error
            Timber.e(throwable, "Error occurred during the cleanup of temporary image files")
            // Return as failed
            Result.failure()
        }
    }
}