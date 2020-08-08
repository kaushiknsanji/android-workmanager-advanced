package com.example.background.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.background.OUTPUT_PATH
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Kotlin file of Utility functions for Testing.
 *
 * @author Kaushik N Sanji
 */

/**
 * Copies file with the given [filename] from the [Context.getAssets] folder in the [testContext]
 * to the [OUTPUT_PATH] in the [targetContext].
 *
 * @param testContext Instrumented Test [Context]
 * @param targetContext Application [Context]
 * @param filename [String] name of the source asset image file
 * @return [Uri] of the temporary file copied to the [OUTPUT_PATH]
 * @throws FileNotFoundException if output file cannot be found/created
 * @throws IOException if the source asset [filename] failed to open for streaming
 */
@Throws(FileNotFoundException::class, IOException::class)
fun copyFileFromTestToTarget(testContext: Context, targetContext: Context, filename: String): Uri {
    // Name of the Temporary PNG file for storing the blurred result of an image
    val name = String.format("blur-filter-output-%s.png", UUID.randomUUID().toString())
    // Output Directory where the above file will be written to
    val outputDir = File(targetContext.filesDir, OUTPUT_PATH)
    // Create the above directory if it does not exist
    if (!outputDir.exists()) {
        outputDir.mkdirs() // should succeed
    }
    // Create the Temporary PNG File in the above directory
    val outputFile = File(outputDir, name)

    // Read the Source Bitmap of the assets file
    val sourceBitmap = BitmapFactory.decodeStream(testContext.assets.open(filename))
    // Write the Source Bitmap to the above PNG file
    FileOutputStream(outputFile).use { out: FileOutputStream ->
        sourceBitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out)
    }

    // Return the File URI to the above PNG file
    return Uri.fromFile(outputFile)
}

/**
 * Checks if a file pointed to by the [uriString] exists in the [targetContext].
 *
 * @param targetContext Application [Context]
 * @param uriString [String] URI of the Image file. Can be `null` or empty.
 * @return `true` if the file pointed to by the [uriString] exists; `false` if the file does
 * not exist or the URI is invalid.
 */
fun uriFileExists(targetContext: Context, uriString: String?): Boolean {
    if (!uriString.isNullOrEmpty()) {
        // When the URI of the Image file exists

        return try {
            // Try and create a bitmap by decoding the Image from the URI
            BitmapFactory.decodeStream(targetContext.contentResolver.openInputStream(Uri.parse(uriString)))
            // Return true when the image was decoded successfully
            true
        } catch (e: Exception) {
            // Return false when there is an exception while decoding the image from the URI
            false
        }
    }
    // Return false when the URI of the Image file is invalid
    return false
}