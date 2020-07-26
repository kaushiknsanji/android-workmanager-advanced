/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.*
import com.example.background.workers.BlurWorker
import com.example.background.workers.CleanupWorker
import com.example.background.workers.SaveImageToFileWorker

/**
 * [AndroidViewModel] subclass for [BlurActivity].
 *
 * @param application [Application] instance required for initializing a WorkManager.
 *
 * @author Kaushik N Sanji
 */
class BlurViewModel(application: Application) : AndroidViewModel(application) {

    // URI to the Source Image file
    internal var imageUri: Uri? = null

    // URI to the Output Blurred Image file
    internal var outputUri: Uri? = null

    // Instance of WorkManager
    private val workManager = WorkManager.getInstance(application).also {
        // Clean any previously finished work information
        it.pruneWork()
    }

    // LiveData for SaveToImageFileWorker's WorkInfo objects to retrieve its status and output Data
    val outputWorkInfos: LiveData<List<WorkInfo>> = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     *
     * @param blurLevel The amount to blur the image
     */
    internal fun applyBlur(blurLevel: Int) {
        // Create a one-off work request for cleaning any temporary image files
        val cleanUpRequest = OneTimeWorkRequest.from(CleanupWorker::class.java)

        // Create a one-off work request for applying the Blur filter on the Image
        // with the blur level specified
        val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
                // Input Data containing the Image to blur and the blur level to apply
                .setInputData(createInputDataForUri(blurLevel))
                .build()

        // Configure charging constraint and available storage constraint for SaveImageToFileWorker
        val saveImageToFileConstraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresStorageNotLow(true)
                .build()

        // Create a one-off work request for saving the blurred image to MediaStore filesystem
        val saveImageToFileRequest = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
                .addTag(TAG_OUTPUT) // Use Tag to get its status and output Data
                .setConstraints(saveImageToFileConstraints) // Add Constraints to be satisfied for Work
                .build()

        // Execute clean up first
        workManager.beginUniqueWork(
                IMAGE_MANIPULATION_WORK_NAME, // A unique name identifying this chain of work
                ExistingWorkPolicy.REPLACE, // stop and replace the current work chain if any
                cleanUpRequest
        )
                .then(blurRequest) // then, blur the selected image
                .then(saveImageToFileRequest) // then, save the blurred image
                .enqueue() // enqueue and schedule the chain of work in the background thread
    }

    /**
     * Parses and converts [uriString] into [Uri] instance.
     *
     * @param uriString [String] instance containing the URI to a file.
     */
    private fun uriOrNull(uriString: String?): Uri? =
            uriString.takeIf { !it.isNullOrEmpty() }?.let { Uri.parse(it) }

    /**
     * Sets the [imageUri] to the Source Image file given by the [uri] String.
     */
    internal fun setImageUri(uri: String?) {
        imageUri = uriOrNull(uri)
    }

    /**
     * Sets the [outputUri] to the Blurred Image file.
     */
    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }

    /**
     * Creates and returns the input [Data] object containing
     * the [imageUri] of the Image to operate on.
     *
     * @param blurLevel The amount to blur the image
     */
    private fun createInputDataForUri(blurLevel: Int): Data = workDataOf(
            // Uri to the Image to be blurred
            KEY_IMAGE_URI to imageUri.toString(),
            // Level of blur to be applied on the Image
            KEY_BLUR_LEVEL to blurLevel
    )

    /**
     * Called when the user clicks on the "Cancel" button to cancel the ongoing work.
     * Issues cancellation of all the unfinished work identified by the
     * [IMAGE_MANIPULATION_WORK_NAME] unique work chain.
     */
    internal fun cancelWork() {
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

}
