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

@file:JvmName("WorkerUtils")

package com.example.background.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur
import com.example.background.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Create a Notification that is shown as a heads-up notification if possible.
 *
 * For this codelab, this is used to show a notification so that you know when different steps
 * of the background work chain are starting
 *
 * @param context [Context] needed for adding Notification Channel and creating the Notification.
 * @param message Message shown on the notification
 */
fun makeStatusNotification(context: Context, message: String) {

    // Make a channel if necessary
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
        val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description

        // Add the channel
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }

    // Create the notification
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

    // Show the notification
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
}

/**
 * Method for sleeping for a fixed about of time to emulate slower work
 */
fun sleep() {
    try {
        Thread.sleep(DELAY_TIME_MILLIS, 0)
    } catch (exception: InterruptedException) {
        Timber.e(exception)
    }
}

/**
 * Blurs the given Bitmap image with the [blurLevel] specified.
 * @param applicationContext Application [Context] to initialize [RenderScript]
 * @param bitmapToBlur [Bitmap] Image to blur
 * @param blurLevel The amount to blur the image
 * @return Blurred [Bitmap] image
 */
@WorkerThread
fun blurBitmap(applicationContext: Context, bitmapToBlur: Bitmap, blurLevel: Int): Bitmap {
    // Initialize the Renderscript
    val rsContext: RenderScript = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
    // Initialize the Blur script with the data element for ARGB Pixel data
    val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext)).apply {
        // Set the radius of the Gaussian Blur to apply
        setRadius(10f)
    }

    try {
        // Saves the final result of blur on the "bitmapToBlur" image
        var blurredBitmap = bitmapToBlur

        // Repeat blur filter application for the number of blur levels
        repeat(blurLevel) {
            // Execute the blur and save the result
            blurredBitmap = doBlurBitmap(
                    rsContext,
                    scriptIntrinsicBlur,
                    blurredBitmap
            )
        }

        // Return the blurred bitmap
        return blurredBitmap
    } finally {
        // Release the resources held by RenderScript
        rsContext.finish()
    }
}

/**
 * Invoked by [.blurBitmap] multiple times based on the `blurLevel` chosen by the user.
 * Gaussian blur on the [bitmapToBlur] image is applied that many times.
 *
 * @param rsContext Pre-initialized [RenderScript] instance to create [Allocation]s
 * @param scriptIntrinsicBlur Pre-initialized [ScriptIntrinsicBlur] instance to execute blur
 * @param bitmapToBlur [Bitmap] Image to blur
 * @return Blurred [Bitmap] image
 */
private fun doBlurBitmap(rsContext: RenderScript,
                         scriptIntrinsicBlur: ScriptIntrinsicBlur,
                         bitmapToBlur: Bitmap): Bitmap {
    // Create the output bitmap that will hold the blurred image
    val blurredBitmap = bitmapToBlur.copy(bitmapToBlur.config, true)

    // Create Allocations for Renderscript to run
    val inAlloc = Allocation.createFromBitmap(rsContext, bitmapToBlur)
    val outAlloc = Allocation.createTyped(rsContext, inAlloc.type)

    scriptIntrinsicBlur.apply {
        // Set the Allocation input for the Blur script
        scriptIntrinsicBlur.setInput(inAlloc)
        // Execute the Blur process
        scriptIntrinsicBlur.forEach(outAlloc)
    }

    // Copy the result to the output bitmap
    outAlloc.copyTo(blurredBitmap)

    // Recycle the "bitmapToBlur" as we are returning a new one
    bitmapToBlur.recycle()

    // Release resources held by allocations only
    inAlloc.destroy()
    outAlloc.destroy()

    // Return the blurred bitmap
    return blurredBitmap
}

/**
 * Writes bitmap to a temporary file and returns the Uri for the file
 * @param applicationContext Application context
 * @param bitmap Bitmap to write to temp file
 * @return Uri for temp file with bitmap
 * @throws FileNotFoundException Throws if bitmap file cannot be found
 */
@Throws(FileNotFoundException::class)
fun writeBitmapToFile(applicationContext: Context, bitmap: Bitmap): Uri {
    // Name of the Temporary PNG file for storing the blurred result of an image
    val name = String.format("blur-filter-output-%s.png", UUID.randomUUID().toString())
    // Output Directory where the above file will be written to
    val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)
    // Create the above directory if it does not exist
    if (!outputDir.exists()) {
        outputDir.mkdirs() // should succeed
    }
    // Create the Temporary PNG File in the above directory
    val outputFile = File(outputDir, name)

    // Write the bitmap to the above PNG file
    FileOutputStream(outputFile).use { out: FileOutputStream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out)
    }

    // Return the File URI to the above PNG file
    return Uri.fromFile(outputFile)
}

/**
 * Deletes all temporary image (PNG) files, previously created for capturing
 * the blurred version of the original image, present in the
 * "blur_filter_outputs" subdirectory of the Application's "files" directory.
 *
 * @param applicationContext Application [Context] to access Application's "files" directory.
 */
fun cleanUpTempFiles(applicationContext: Context) {
    // Output Directory where the temporary image files are present
    val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)
    // Check if the Output Directory exists
    if (outputDir.exists()) {
        // When the Output Directory exists
        // Get all PNG files in the folder and delete them
        outputDir.listFiles()?.forEach { file: File? ->
            file?.takeIf { it.name.isNotEmpty() && it.name.endsWith(".png") }?.run {
                // Delete the temporary PNG file
                val isFileDeleted = delete()
                // Log the delete result
                Timber.i("Deleted $name - $isFileDeleted")
            }
        }
    }
}

/**
 * Saves the Image pointed to by the given [bitmapToSaveUriStr],
 * to the device's MediaStore filesystem (like "Pictures" folder).
 *
 * @param applicationContext Application [Context] to get the [android.content.ContentResolver]
 * to decode the [bitmapToSaveUriStr]
 * @param bitmapToSaveUriStr [String] containing the URI to the Image to be saved to the Media Images.
 * @return [String] containing the URI to the newly created image; or `null` if [bitmapToSaveUriStr]
 * was `null` or empty, or if the image failed to be stored for any reason.
 * @throws FileNotFoundException if the provided [bitmapToSaveUriStr] could not be opened.
 */
@Throws(FileNotFoundException::class)
fun saveImageToMedia(applicationContext: Context, bitmapToSaveUriStr: String?): String? {
    // Do not do anything if the Uri string is invalid or empty
    if (bitmapToSaveUriStr.isNullOrEmpty()) return null

    // Get the ContentResolver instance
    val contentResolver = applicationContext.contentResolver

    // Get the Bitmap to be saved from the given Uri
    val bitmapToSave = BitmapFactory.decodeStream(
            contentResolver.openInputStream(Uri.parse(bitmapToSaveUriStr))
    )

    // Get the Date Formatter
    val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    // Write the Image file to MediaStore filesystem and return its Uri String
    return MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmapToSave,
            TITLE_IMAGE,
            dateFormatter.format(Date())
    )
}