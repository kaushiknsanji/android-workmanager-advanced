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
import androidx.work.Configuration
import timber.log.Timber
import timber.log.Timber.DebugTree

/**
 * [Application] subclass to initialize [Timber] logger for the app and
 * to provide the custom [Configuration] for [androidx.work.WorkManager].
 */
class BlurApplication() : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber logger for Debug BuildType only
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

    }

    /**
     * A class that can provide the [Configuration] for WorkManager and allow for on-demand
     * initialization of WorkManager
     *
     * @return The [Configuration] used to initialize WorkManager
     */
    override fun getWorkManagerConfiguration(): Configuration = if (BuildConfig.DEBUG) {
        // Use Logging Level of DEBUG for DEBUG Build Type
        Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build()
    } else {
        // Use Logging Level of ERROR for other Build Types
        Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.ERROR)
                .build()
    }

}
