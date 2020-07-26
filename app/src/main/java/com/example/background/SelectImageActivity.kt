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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.background.databinding.ActivitySelectBinding
import timber.log.Timber

/**
 * Starting activity of the app that inflates the layout 'R.layout.activity_select'
 * to allow the user to pick an image to be blurred later.
 *
 * @author Kaushik N Sanji
 */
class SelectImageActivity : AppCompatActivity() {

    companion object {
        // Intent request constant for Picking an Image
        const val REQUEST_CODE_IMAGE = 100

        // Permission request constant for External storage access
        const val REQUEST_CODE_PERMISSIONS = 101

        // Bundle Constant to save the count of permission requests retried
        const val KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT"

        // Constant to limit the number of permission request retries
        const val MAX_NUMBER_REQUEST_PERMISSIONS = 2
    }

    // List of permissions required by the app to access external storage
    // to allow the user to select an image
    private val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // Stores the count of permission requests retried
    private var permissionRequestCount: Int = 0

    // ViewBinding instance for this activity
    private lateinit var binding: ActivitySelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate and bind the layout 'R.layout.activity_select'
        binding = ActivitySelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // When activity is reloaded after configuration change
        savedInstanceState?.let {
            // Restore the permission request count
            permissionRequestCount = it.getInt(KEY_PERMISSIONS_REQUEST_COUNT, 0)
        }

        // Make sure the app has correct permissions to run
        requestPermissionsIfNecessary()

        // Create request to get image from filesystem when button clicked
        binding.selectImage.setOnClickListener {
            val chooseIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            // Start the activity for picking an Image
            startActivityForResult(chooseIntent, REQUEST_CODE_IMAGE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the permission request count on rotation
        outState.putInt(KEY_PERMISSIONS_REQUEST_COUNT, permissionRequestCount)
    }

    /**
     * Request permissions twice - if the user denies twice then show a toast about how to update
     * the permission for storage. Also disable the button if we don't have access to pictures on
     * the device.
     */
    private fun requestPermissionsIfNecessary() {
        // Check if all required permissions are granted
        if (!checkAllPermissions()) {
            // When all required permissions are not granted yet

            if (permissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                // When the number of permission request retried is less than the max limit set
                permissionRequestCount += 1 // Increment the number of permission requests done
                // Request the required permissions for external storage access
                ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        REQUEST_CODE_PERMISSIONS
                )
            } else {
                // When the number of permission request retried exceeds the max limit set
                // Show a toast about how to update the permission for storage access
                Toast.makeText(
                        this,
                        R.string.set_permissions_in_settings,
                        Toast.LENGTH_LONG
                ).show()
                // Disable the "Select Image" button when access is denied by the user
                binding.selectImage.isEnabled = false
            }
        }
    }

    /**
     * Method that checks if all the required permissions are granted.
     *
     * @return `true` if all the required permissions are granted; `false` otherwise.
     */
    private fun checkAllPermissions(): Boolean {
        // Boolean state to indicate all permissions are granted
        var hasPermissions = true
        // Verify all permissions are granted
        for (permission in permissions) {
            hasPermissions = hasPermissions and isPermissionGranted(permission)
        }
        // Return the state of all permissions granted
        return hasPermissions
    }

    /**
     * Checks if the given [permission] is granted.
     *
     * @return `true` if the given [permission] is granted; `false` otherwise.
     */
    private fun isPermissionGranted(permission: String) =
            ContextCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_GRANTED

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on [.requestPermissions].
     *
     * @param requestCode The request code passed in [.requestPermissions].
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [android.content.pm.PackageManager.PERMISSION_GRANTED]
     * or [android.content.pm.PackageManager.PERMISSION_DENIED]. Never null.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            // For External Storage access permission request
            REQUEST_CODE_PERMISSIONS -> requestPermissionsIfNecessary() // no-op if permissions are granted already.
            // For other requests, delegate to super
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * Called when an activity you launched exits, giving you the [requestCode]
     * you started it with, the [resultCode] it returned, and any additional
     * Intent [data] from it.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            // When we have a success result from any Activity started

            when (requestCode) {
                // For Image Pick request
                REQUEST_CODE_IMAGE -> data?.let {
                    handleImageRequestResult(data)
                }
                // For unknown requests, log the unknown code
                else -> Timber.d("Unknown request code %d", requestCode)
            }
        } else {
            // Log the error for unexpected result
            Timber.e(String.format("Unexpected Result code %d", resultCode))
        }
    }

    /**
     * Method invoked on the result of Image Pick request.
     * Starts the [BlurActivity] with the URI of the Image Picked.
     *
     * @param intent Resulting [Intent] of the Image Pick request containing the URI of the Image Picked.
     */
    private fun handleImageRequestResult(intent: Intent) {
        // Get the URI of the Image picked
        val imageUri: Uri? = intent.clipData?.let {
            // Get the URI from the Intent's ClipData if present
            it.getItemAt(0).uri
        } ?: intent.data //else, get the URI from the Intent's data

        // If there is no URI available, log the error and return.
        if (imageUri == null) {
            Timber.e("Invalid input image Uri.")
            return
        }

        // Start the BlurActivity with the URI of the Image Picked
        startActivity(Intent(this, BlurActivity::class.java).apply {
            putExtra(KEY_IMAGE_URI, imageUri.toString())
        })
    }
}
