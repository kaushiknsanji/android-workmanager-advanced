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

@file:JvmName("Constants")

package com.example.background

// Notification Channel constants
// Name of Notification Channel for verbose notifications of background work
const val VERBOSE_NOTIFICATION_CHANNEL_NAME =
        "Verbose WorkManager Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
        "Shows notifications whenever work starts"

const val NOTIFICATION_TITLE = "WorkRequest Starting"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 1

// The name of the image manipulation work
const val IMAGE_MANIPULATION_WORK_NAME = "image_manipulation_work"

// Other keys
const val OUTPUT_PATH = "blur_filter_outputs"
const val KEY_IMAGE_URI = "KEY_IMAGE_URI"
const val KEY_BLUR_LEVEL = "KEY_BLUR_LEVEL"
const val TAG_OUTPUT = "OUTPUT"
const val TITLE_IMAGE = "BlurredImage"
const val DATE_FORMAT = "yyyy.MM.dd 'at' HH:mm:ss z"

// Delay to slow down each WorkRequest so that it
// becomes easier to identify each WorkRequest start
const val DELAY_TIME_MILLIS: Long = 3000
