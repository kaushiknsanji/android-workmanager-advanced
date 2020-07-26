package com.example.background.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import timber.log.Timber

/**
 * [androidx.work.ListenableWorker] subclass that implements [Worker.doWork] to provide the code
 * for saving an Image to MediaStore filesystem. Image to be saved is provided as [WorkerParameters.mInputData]
 * to the Worker from the previous [BlurWorker] in the chain.
 *
 * @param context The application [Context]
 * @param workerParameters Parameters to setup the internal state of this worker
 *
 * @author Kaushik N Sanji
 */
class SaveImageToFileWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    /**
     * Override this method to do your actual background processing.  This method is called on a
     * background thread - you are required to **synchronously** do your work and return the
     * [androidx.work.ListenableWorker.Result] from this method.  Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed. If
     * you need to do your work asynchronously on a thread of your own choice, see
     * [androidx.work.ListenableWorker].
     *
     * A Worker is given a maximum of ten minutes to finish its execution and return a
     * [androidx.work.ListenableWorker.Result]. After this time has expired, the Worker will
     * be signalled to stop.
     *
     * @return The [androidx.work.ListenableWorker.Result] of the computation; note that
     * dependent work will not execute if you use
     * [androidx.work.ListenableWorker.Result.failure] or
     * [androidx.work.ListenableWorker.Result.failure]
     */
    override fun doWork(): Result {
        // Show a Notification when the work starts
        makeStatusNotification(applicationContext, "Saving Image...")
        // Slow down the start so that it is easier to see each WorkRequest start
        sleep()

        return try {
            // Try saving the image to MediaStore filesystem
            saveImageToMedia(
                    applicationContext,
                    inputData.getString(KEY_IMAGE_URI))?.let { imageUriStr: String ->
                // On Success

                // Return as successful with the output Data containing the Uri
                // to the permanently saved blurred image file, in order to make it available
                // to other workers for further operations
                Result.success(workDataOf(KEY_IMAGE_URI to imageUriStr))
            } ?: run {
                // On Failure to save the image

                // Log the error
                Timber.e("Error: Image could not be saved to MediaStore")
                // Return as failed
                Result.failure()
            }
        } catch (throwable: Throwable) {
            // Log the error
            Timber.e(throwable, "Error occurred while saving image to MediaStore")
            // Return as failed
            Result.failure()
        }
    }
}