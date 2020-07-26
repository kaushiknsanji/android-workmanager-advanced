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

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.background.databinding.ActivityBlurBinding

/**
 * Activity that inflates the layout 'R.layout.activity_blur' to allow the user
 * to apply the Blur filter on the Image shown.
 *
 * @author Kaushik N Sanji
 */
class BlurActivity : AppCompatActivity() {

    // Initialize the BlurViewModel
    private val viewModel by viewModels<BlurViewModel>()

    // ViewBinding instance for this activity
    private lateinit var binding: ActivityBlurBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate and bind the layout 'R.layout.activity_blur'
        binding = ActivityBlurBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Image URI should be stored in the ViewModel; put it there then display
        val imageUriExtra = intent.getStringExtra(KEY_IMAGE_URI)
        // Save the URI of the Image picked to the ViewModel
        viewModel.setImageUri(imageUriExtra)
        // Load the Image picked
        viewModel.imageUri?.let { imageUri ->
            Glide.with(this).load(imageUri).into(binding.imageView)
        }

        // Register a click listener on the "Go" button
        binding.goButton.setOnClickListener {
            // Delegate to the ViewModel to apply the Blur filter on the Image with the chosen Blur level
            viewModel.applyBlur(blurLevel)
        }

        // Register a click listener on the "See File" button
        binding.seeFileButton.setOnClickListener {
            // Create an Intent to view the Image pointed to by the Output URI saved in the ViewModel
            Intent(Intent.ACTION_VIEW, viewModel.outputUri).let { actionViewIntent ->
                // Check if there is any activity to handle this Intent
                actionViewIntent.resolveActivity(packageManager)?.run {
                    // When we have found an activity, start the activity with the Intent
                    startActivity(actionViewIntent)
                }
            }
        }

        // Register an observer on the SaveToImageFileWorker's WorkInfo objects LiveData to retrieve
        // its status and output Data
        viewModel.outputWorkInfos.observe(this, Observer { workInfos ->
            if (!workInfos.isNullOrEmpty()) {
                // When WorkInfo Objects are generated

                // Pick the first WorkInfo object. There will be only one WorkInfo object
                // since the corresponding WorkRequest that was tagged is part of a unique work chain
                val workInfo = workInfos[0]

                // Check the work status
                if (workInfo.state.isFinished) {
                    // When the work is finished (i.e., SUCCEEDED / FAILED / CANCELLED),
                    // show and hide the appropriate views for the same
                    showWorkFinished()

                    // Read the final output Image URI string from the WorkInfo's Output Data
                    workInfo.outputData.getString(KEY_IMAGE_URI)
                            .takeIf { !it.isNullOrEmpty() }?.let { outputUriStr ->
                                // When we have the final Image URI

                                // Save the final Image URI string in the ViewModel
                                viewModel.setOutputUri(outputUriStr)
                                // Show the "See File" button
                                binding.seeFileButton.visibility = View.VISIBLE
                            }

                } else {
                    // In other cases, show and hide the appropriate views for the same
                    showWorkInProgress()
                }
            }
        })

        // Register a click listener on the "Cancel" button
        binding.cancelButton.setOnClickListener {
            // Delegate to the ViewModel to cancel unfinished work
            viewModel.cancelWork()
        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            goButton.visibility = View.GONE
            seeFileButton.visibility = View.GONE
        }
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        with(binding) {
            progressBar.visibility = View.GONE
            cancelButton.visibility = View.GONE
            goButton.visibility = View.VISIBLE
        }
    }

    // Retrieves the Blur level chosen by the user
    private val blurLevel: Int
        get() =
            when (binding.radioBlurGroup.checkedRadioButtonId) {
                R.id.radio_blur_lv_1 -> 1
                R.id.radio_blur_lv_2 -> 2
                R.id.radio_blur_lv_3 -> 3
                else -> 1
            }
}
