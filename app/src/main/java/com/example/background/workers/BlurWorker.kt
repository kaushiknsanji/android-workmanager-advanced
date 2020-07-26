package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_BLUR_LEVEL
import com.example.background.KEY_IMAGE_URI
import timber.log.Timber

/**
 * [androidx.work.ListenableWorker] subclass that implements [Worker.doWork] to provide the code
 * for applying the blur filter on an Image in a background process.
 *
 * @param context The application [Context]
 * @param workerParams Parameters to setup the internal state of this worker
 *
 * @author Kaushik N Sanji
 */
class BlurWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    /**
     * This method is called on a background thread. Required work must be done **synchronously**
     * and should return the [androidx.work.ListenableWorker.Result] from this method. Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed.  If
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
     * [androidx.work.ListenableWorker.Result.failure(data)]
     */
    override fun doWork(): Result {
        // Get App Context
        val appContext = applicationContext

        // Get the Input Data sent
        val pictureToBlurUriStr = inputData.getString(KEY_IMAGE_URI)

        // Get the level of Blur to be applied
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

        // Show a Notification before starting the work for blurring the image
        makeStatusNotification(appContext, "Blurring Image...")

        // Slow down the start so that it is easier to see each WorkRequest start
        sleep()

        return try {
            // If the Uri of the Image to be blurred is invalid/empty, then log the error
            // and throw an exception
            if (pictureToBlurUriStr.isNullOrEmpty()) {
                Timber.e("Invalid input Uri $pictureToBlurUriStr")
                throw IllegalArgumentException("Invalid input Uri $pictureToBlurUriStr")
            }

            // Get the ContentResolver instance
            val contentResolver = appContext.contentResolver

            // Get the Picture to be blurred
            val pictureToBlur = BitmapFactory
                    .decodeStream(contentResolver.openInputStream(Uri.parse(pictureToBlurUriStr)))

            // Apply the blur filter on the Image
            val blurredPicture = blurBitmap(appContext, pictureToBlur, blurLevel)

            // Write the result to a temporary image file
            val blurredPictureUri = writeBitmapToFile(appContext, blurredPicture)

            // Return as successful with the output Data containing the Uri
            // to the temporary blurred image file, in order to make it available
            // to other workers for further operations
            Result.success(workDataOf(KEY_IMAGE_URI to blurredPictureUri.toString()))
        } catch (throwable: Throwable) {
            // Log the error
            Timber.e(throwable, "Error occurred while applying blur")
            // Return as failed
            Result.failure()
        }
    }
}